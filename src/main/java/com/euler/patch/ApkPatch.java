//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.euler.patch;

import brut.androlib.mod.SmaliMod;
import com.euler.patch.diff.DexDiffer;
import com.euler.patch.diff.DiffInfo;
import com.euler.patch.utils.Formater;
import com.euler.patch.utils.TypeGenUtil;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.io.FileUtils;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.util.SyntheticAccessorResolver;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.util.ClassFileNameHandler;
import org.jf.util.IndentingWriter;

import java.io.*;
import java.sql.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by habbyge on 2021/1/5.
 * 修改点：新增的class，不会生成_CF版本，直接以原始名称打包在patch中
 */
public class ApkPatch extends Build {
    private File from;
    private File to;
    private Set<String> classes;

    public ApkPatch(File from, File to, String name, File out, String keystore,
                    String password, String alias, String entry) {

        super(name, out, keystore, password, alias, entry);
        this.from = from;
        this.to = to;
    }

    public void doPatch() {
        try {
            File smaliDir = new File(this.out, "smali");
            if (!smaliDir.exists()) {
                smaliDir.mkdir();
            }

            try {
                FileUtils.cleanDirectory(smaliDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            File dexFile = new File(this.out, "diff.dex");
            if (dexFile.exists() && !dexFile.delete()) {
                throw new RuntimeException("diff.dex can't be removed.");
            }

            File outFile = new File(this.out, "diff.apatch");
            if (outFile.exists() && !outFile.delete()) {
                throw new RuntimeException("diff.apatch can't be removed.");
            }

            DiffInfo info = (new DexDiffer()).diff(this.from, this.to);
            this.classes = buildCode(smaliDir, dexFile, info);
            this.build(outFile, dexFile);
            this.release(this.out, dexFile, outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Set<String> buildCode(File smaliDir, File dexFile, DiffInfo info)
            throws IOException, RecognitionException {

        Set<DexBackedClassDef> list = new HashSet<>();
        list.addAll(info.getAddedClasses()); // 新增的class
        list.addAll(info.getModifiedClasses());

        baksmaliOptions options = new baksmaliOptions();
        options.deodex = false;
        options.noParameterRegisters = false;
        options.useLocalsDirective = true;
        options.useSequentialLabels = true;
        options.outputDebugInfo = true;
        options.addCodeOffsets = false;
        options.jobs = -1;
        options.noAccessorComments = false;
        options.registerInfo = 0;
        options.ignoreErrors = false;
        options.inlineResolver = null;
        options.checkPackagePrivateAccess = false;
        options.syntheticAccessorResolver = new SyntheticAccessorResolver(list);

        ClassFileNameHandler outFileNameHandler = new ClassFileNameHandler(smaliDir, ".smali");
        ClassFileNameHandler inFileNameHandler = new ClassFileNameHandler(smaliDir, ".smali");
        DexBuilder dexBuilder = DexBuilder.makeDexBuilder();

        Set<String> classes = new HashSet<>();

        Iterator<DexBackedClassDef> it = list.iterator();
        while (it.hasNext()) {
            DexBackedClassDef classDef = it.next();
            String className = classDef.getType();

            /*baksmali.disassembleClass(classDef, outFileNameHandler, options);*/

            String newClassName;
            // 修改点：新增的class，不会生成_CF版本，直接以原始名称打包在patch中
            if (info.inAddedClasses(className)) {
                newClassName = className;
                System.out.println("iWatch-HABBYGE-MALI, new class: " + className);
            } else {
                newClassName = TypeGenUtil.newType(className);
                System.out.println("iWatch-HABBYGE-MALI, modified class: " + className);
            }
            disassembleClass(classDef, outFileNameHandler, options, newClassName); // TODO: 2021/3/16

            File smaliFile = inFileNameHandler.getUniqueFilenameForClass(newClassName);
            if (!smaliFile.exists()) {
                System.err.println("iWatch-HABBYGE-MALI, NOT exist: "
                        + smaliFile.getName() + ", " + className);

                continue;
            }
            System.out.println("iWatch-HABBYGE-MALI: exist: " + smaliFile.getName() + ", " + className);

            classes.add(newClassName.substring(1, newClassName.length() - 1).replace('/', '.'));

            SmaliMod.assembleSmaliFile(smaliFile, dexBuilder, true, true);
            System.out.println("iWatch-HABBYGE-MALI: Success !");
        }

        dexBuilder.writeTo(new FileDataStore(dexFile));
        return classes;
    }

    protected Manifest getMeta() {
        Manifest manifest = new Manifest();
        Attributes main = manifest.getMainAttributes();
        main.putValue("Manifest-Version", "1.0");
        main.putValue("Created-By", "1.0 (ApkPatch)");
        main.putValue("Created-Time", (new Date(System.currentTimeMillis())).toGMTString());
        main.putValue("From-File", this.from.getName());
        main.putValue("To-File", this.to.getName());
        main.putValue("Patch-Name", this.name);
        main.putValue("Patch-Classes", Formater.dotStringList(this.classes));
        return manifest;
    }

    private static void disassembleClass(ClassDef classDef,
                                         ClassFileNameHandler fileNameHandler,
                                         baksmaliOptions options,
                                         String newClassName) {

        if (newClassName.charAt(0) == 'L' && newClassName.charAt(newClassName.length() - 1) == ';') {
            File smaliFile = fileNameHandler.getUniqueFilenameForClass(newClassName);
            ClassDefinition classDefinition = new ClassDefinition(options, classDef, newClassName);
            IndentingWriter writer = null;

            try {
                File smaliParent = smaliFile.getParentFile();
                if (smaliParent.exists() || smaliParent.mkdirs() || smaliParent.exists()) {
                    if (!smaliFile.exists() && !smaliFile.createNewFile()) {
                        System.err.println("Unable to create file " + smaliFile.toString() + " - skipping class");
                        return;
                    }

                    BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(smaliFile), "UTF8"));

                    writer = new IndentingWriter(bufWriter);
                    classDefinition.writeTo(writer);
                    return;
                }

                System.err.println("Unable to create directory " + smaliParent.toString() + " - skipping class");
            } catch (Exception e) {
                System.err.println("\n\nError occurred while disassembling class " +
                        newClassName.replace('/', '.') + " - skipping class");

                e.printStackTrace();
                smaliFile.delete();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable e) {
                        System.err.println("\n\nError occurred while closing file " + smaliFile.toString());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("Unrecognized class descriptor - " + newClassName + " - skipping class");
        }
    }
}
