//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.jf.baksmali.Adaptors;

import com.euler.patch.annotation.MethodReplaceAnnotaion;
import com.euler.patch.diff.DiffInfo;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.jf.baksmali.baksmaliOptions;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.dexbacked.DexBackedDexFile.InvalidItemIndex;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.util.ReferenceUtil;
import org.jf.util.IndentingWriter;
import org.jf.util.StringUtils;

public class ClassDefinition {
    @Nonnull
    public final baksmaliOptions options;
    @Nonnull
    public final ClassDef classDef;
    @Nonnull
    private final HashSet<String> fieldsSetInStaticConstructor;

    private String mRealName = null;

    protected boolean validationErrors;

//    public ClassDefinition(@Nonnull baksmaliOptions options, @Nonnull ClassDef classDef) {
//        this.options = options; // todo
//        this.classDef = classDef;
//        this.fieldsSetInStaticConstructor = this.findFieldsSetInStaticConstructor();
//    }

    public ClassDefinition(@Nonnull baksmaliOptions options, @Nonnull ClassDef classDef, String realName) {
        this.options = options;
        this.classDef = classDef;
        this.fieldsSetInStaticConstructor = this.findFieldsSetInStaticConstructor();
        this.mRealName = realName;
    }

    public boolean hadValidationErrors() {
        return this.validationErrors;
    }

    @Nonnull
    private HashSet<String> findFieldsSetInStaticConstructor() {
        HashSet<String> fieldsSetInStaticConstructor = new HashSet<>();

        for (Method method : classDef.getDirectMethods()) {
            if (method.getName().equals("<clinit>")) {
                MethodImplementation impl = method.getImplementation();
                if (impl != null) {
                    for (Instruction instruction: impl.getInstructions()) {
                        switch (instruction.getOpcode()) {
                        case SPUT:
                        case SPUT_BOOLEAN:
                        case SPUT_BYTE:
                        case SPUT_CHAR:
                        case SPUT_OBJECT:
                        case SPUT_SHORT:
                        case SPUT_WIDE: {
                            Instruction21c ins = (Instruction21c) instruction;
                            FieldReference fieldRef = null;
                            try {
                                fieldRef = (FieldReference)ins.getReference();
                            } catch (InvalidItemIndex ex) {
                                // just ignore it for now. We'll deal with it later, when processing the instructions
                                // themselves
                            }
                            if (fieldRef != null && fieldRef.getDefiningClass().equals((classDef.getType()))) {
                                fieldsSetInStaticConstructor.add(ReferenceUtil.getShortFieldDescriptor(fieldRef));
                            }
                            break;
                        }

                        }
                    }
                }
            }
        }
        return fieldsSetInStaticConstructor;
    }

    public void writeTo(IndentingWriter writer) throws IOException {
        this.writeClass(writer);
        this.writeSuper(writer);
        this.writeSourceFile(writer);
        this.writeInterfaces(writer);
        this.writeAnnotations(writer);
        Set<String> staticFields = this.writeStaticFields(writer);
        this.writeInstanceFields(writer, staticFields);
        Set<String> directMethods = this.writeDirectMethods(writer);
        this.writeVirtualMethods(writer, directMethods);
    }

    private void writeClass(IndentingWriter writer) throws IOException {
        writer.write(".class ");
        this.writeAccessFlags(writer);

        if (mRealName == null) {
//            writer.write(TypeGenUtil.newType(this.classDef.getType()));
            writer.write(this.classDef.getType());
        } else { // todo ing......
            writer.write(mRealName);
        }

        writer.write('\n');
    }

    private void writeAccessFlags(IndentingWriter writer) throws IOException {
        for (AccessFlags accessFlag: AccessFlags.getAccessFlagsForClass(classDef.getAccessFlags())) {
            writer.write(accessFlag.toString());
            writer.write(' ');
        }
    }

    private void writeSuper(IndentingWriter writer) throws IOException {
        String superClass = this.classDef.getSuperclass();
        if (superClass != null) {
            writer.write(".super ");
            writer.write(superClass);
            writer.write('\n');
        }
    }

    private void writeSourceFile(IndentingWriter writer) throws IOException {
        String sourceFile = this.classDef.getSourceFile();
        if (sourceFile != null) {
            writer.write(".source \"");
            StringUtils.writeEscapedString(writer, sourceFile);
            writer.write("\"\n");
        }

    }

    private void writeInterfaces(IndentingWriter writer) throws IOException {
        List<String> interfaces = Lists.newArrayList(this.classDef.getInterfaces());
        Collections.sort(interfaces);
        if (interfaces.size() != 0) {
            writer.write('\n');
            writer.write("# interfaces\n");
            Iterator var4 = interfaces.iterator();

            while(var4.hasNext()) {
                String interfaceName = (String)var4.next();
                writer.write(".implements ");
                writer.write(interfaceName);
                writer.write('\n');
            }
        }

    }

    private void writeAnnotations(IndentingWriter writer) throws IOException {
        Collection<? extends Annotation> classAnnotations = this.classDef.getAnnotations();
        if (classAnnotations.size() != 0) {
            writer.write("\n\n");
            writer.write("# annotations\n");
            String containingClass = null;
            if (this.options.useImplicitReferences) {
                containingClass = this.classDef.getType();
            }

            AnnotationFormatter.writeTo(writer, classAnnotations, containingClass);
        }
    }

    private Set<String> writeStaticFields(IndentingWriter writer) throws IOException {
        boolean wroteHeader = false;
        Set<String> writtenFields = new HashSet<String>();

        Iterable<? extends Field> staticFields;
        if (classDef instanceof DexBackedClassDef) {
            staticFields = ((DexBackedClassDef)classDef).getStaticFields(false);
        } else {
            staticFields = classDef.getStaticFields();
        }

        for (Field field: staticFields) {
            if (!wroteHeader) {
                writer.write("\n\n");
                writer.write("# static fields");
                wroteHeader = true;
            }
            writer.write('\n');

            boolean setInStaticConstructor;
            IndentingWriter fieldWriter = writer;
            String fieldString = ReferenceUtil.getShortFieldDescriptor(field);
            if (!writtenFields.add(fieldString)) {
                writer.write("# duplicate field ignored\n");
                fieldWriter = new CommentingIndentingWriter(writer);
                System.err.println(String.format("Ignoring duplicate field: %s->%s", classDef.getType(), fieldString));
                setInStaticConstructor = false;
            } else {
                setInStaticConstructor = fieldsSetInStaticConstructor.contains(fieldString);
            }
            FieldDefinition.writeTo(options, fieldWriter, field, setInStaticConstructor);
        }
        return writtenFields;
    }

    private void writeInstanceFields(IndentingWriter writer, Set<String> staticFields) throws IOException {
        boolean wroteHeader = false;
        Set<String> writtenFields = new HashSet<String>();

        Iterable<? extends Field> instanceFields;
        if (classDef instanceof DexBackedClassDef) {
            instanceFields = ((DexBackedClassDef)classDef).getInstanceFields(false);
        } else {
            instanceFields = classDef.getInstanceFields();
        }

        for (Field field: instanceFields) {
            if (!wroteHeader) {
                writer.write("\n\n");
                writer.write("# instance fields");
                wroteHeader = true;
            }
            writer.write('\n');

            IndentingWriter fieldWriter = writer;
            String fieldString = ReferenceUtil.getShortFieldDescriptor(field);
            if (!writtenFields.add(fieldString)) {
                writer.write("# duplicate field ignored\n");
                fieldWriter = new CommentingIndentingWriter(writer);
                System.err.println(String.format("Ignoring duplicate field: %s->%s",
                        classDef.getType(), fieldString));
            } else if (staticFields.contains(fieldString)) {
                System.err.println(String.format("Duplicate static+instance field found: %s->%s",
                        classDef.getType(), fieldString));
                System.err.println("You will need to rename one of these fields, including all references.");

                writer.write("# There is both a static and instance field with this signature.\n" +
                        "# You will need to rename one of these fields, including all references.\n");
            }
            FieldDefinition.writeTo(options, fieldWriter, field, false);
        }
    }

    private Set<String> writeDirectMethods(IndentingWriter writer) throws IOException {
        boolean wroteHeader = false; // TODO: 2021/3/16 ing
        Set<String> writtenMethods = new HashSet();
        Set<? extends Method> modifieds = null;
        Iterable directMethods;
        if (this.classDef instanceof DexBackedClassDef) {
            directMethods = ((DexBackedClassDef) this.classDef).getDirectMethods(false);
            modifieds = DiffInfo.getInstance().getModifiedMethods();
        } else {
            directMethods = this.classDef.getDirectMethods();
        }

        Iterator it = directMethods.iterator();

        while (it.hasNext()) {
            Method method = (Method) it.next();
            if (modifieds != null && modifieds.contains(method)) {
                MethodReplaceAnnotaion replaceAnnotaion = new MethodReplaceAnnotaion(
                        method.getDefiningClass(), method.getName());

                ((DexBackedMethod) method).setMethodReplace(replaceAnnotaion);
            }

            if (!wroteHeader) {
                writer.write("\n\n");
                writer.write("# direct methods");
                wroteHeader = true;
            }

            writer.write('\n');
            String methodString = ReferenceUtil.getMethodDescriptor(method, true);
            IndentingWriter methodWriter = writer;
            if (!writtenMethods.add(methodString)) {
                writer.write("# duplicate method ignored\n");
                methodWriter = new CommentingIndentingWriter(writer);
            }

            MethodImplementation methodImpl = method.getImplementation();
            if (methodImpl == null) {
                MethodDefinition.writeEmptyMethodTo((IndentingWriter) methodWriter, method, this.options);
            } else {
                MethodDefinition methodDefinition = new MethodDefinition(this, method, methodImpl);
                methodDefinition.writeTo((IndentingWriter) methodWriter);
            }
        }

        return writtenMethods;
    }

    private void writeVirtualMethods(IndentingWriter writer, Set<String> directMethods) throws IOException {
        boolean wroteHeader = false;
        Set<String> writtenMethods = new HashSet();
        Set<? extends Method> modifieds = null;
        Iterable virtualMethods;
        if (this.classDef instanceof DexBackedClassDef) {
            virtualMethods = ((DexBackedClassDef) this.classDef).getVirtualMethods(false);
            modifieds = DiffInfo.getInstance().getModifiedMethods();
        } else {
            virtualMethods = this.classDef.getVirtualMethods();
        }

        Iterator it = virtualMethods.iterator();

        while (it.hasNext()) {
            Method method = (Method) it.next();
            if (modifieds != null && modifieds.contains(method)) {
                MethodReplaceAnnotaion replaceAnnotaion = new MethodReplaceAnnotaion(
                        method.getDefiningClass(), method.getName());

                ((DexBackedMethod) method).setMethodReplace(replaceAnnotaion);
            }

            if (!wroteHeader) {
                writer.write("\n\n");
                writer.write("# virtual methods");
                wroteHeader = true;
            }

            writer.write('\n');
            String methodString = ReferenceUtil.getMethodDescriptor(method, true);
            IndentingWriter methodWriter = writer;
            if (!writtenMethods.add(methodString)) {
                writer.write("# duplicate method ignored\n");
                methodWriter = new CommentingIndentingWriter(writer);
            } else if (directMethods.contains(methodString)) {
                writer.write("# There is both a direct and virtual method with this signature.\n# You will need to rename one of these methods, including all references.\n");
                System.err.println(String.format("Duplicate direct+virtual method found: %s->%s",
                        this.classDef.getType(), methodString));

                System.err.println("You will need to rename one of these methods, including all references.");
            }

            MethodImplementation methodImpl = method.getImplementation();
            if (methodImpl == null) {
                MethodDefinition.writeEmptyMethodTo((IndentingWriter) methodWriter, method, this.options);
            } else {
                MethodDefinition methodDefinition = new MethodDefinition(this, method, methodImpl);
                methodDefinition.writeTo((IndentingWriter) methodWriter);
            }
        }
    }
}
