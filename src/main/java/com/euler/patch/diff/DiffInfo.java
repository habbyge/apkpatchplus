//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.euler.patch.diff;

import com.euler.patch.utils.Formater;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

public class DiffInfo {
    private static final String TAG = "iwatch.DiffInfo:";

    private final static DiffInfo info = new DiffInfo();
    private final Set<DexBackedClassDef> addedClasses = new HashSet<>();
    private final Set<String> addedClassNames = new HashSet<>();
    private final Set<DexBackedClassDef> modifiedClasses = new HashSet<>();

    private final Set<DexBackedField> addedFields = new HashSet<>();
    private final Set<DexBackedField> modifiedFields = new HashSet<>();

    private final Set<DexBackedMethod> addedMethods = new HashSet<>();
    private final Set<DexBackedMethod> modifiedMethods = new HashSet<>();

    private DiffInfo() {
    }

    public static synchronized DiffInfo getInstance() {
        return info;
    }

    public Set<DexBackedClassDef> getAddedClasses() {
        return this.addedClasses;
    }

    public boolean inAddedClasses(String className) {
        return addedClassNames.contains(className);
    }

    public DexBackedClassDef getAddedClasses(String clazz) {
        Iterator it = this.addedClasses.iterator();
        while (it.hasNext()) {
            DexBackedClassDef classDef = (DexBackedClassDef) it.next();
            if (classDef.getType().equals(clazz)) {
                return classDef;
            }
        }
        return null;
    }

    public void addAddedClasses(DexBackedClassDef clazz) {
        System.out.println(TAG + "add new Class:" + clazz.getType());
        this.addedClasses.add(clazz);
        this.addedClassNames.add(clazz.getType());
    }

    public Set<DexBackedClassDef> getModifiedClasses() {
        return this.modifiedClasses;
    }

    public DexBackedClassDef getModifiedClasses(String clazz) {
        Iterator it = this.modifiedClasses.iterator();
        while (it.hasNext()) {
            DexBackedClassDef classDef = (DexBackedClassDef) it.next();
            if (classDef.getType().equals(clazz)) {
                return classDef;
            }
        }
        return null;
    }

    public void addModifiedClasses(DexBackedClassDef clazz) {
        System.out.println(TAG + "add modified Class:" + clazz.getType());
        this.modifiedClasses.add(clazz);
    }

    public Set<DexBackedField> getAddedFields() {
        return this.addedFields;
    }

    public void addAddedFields(DexBackedField field) {
        this.addedFields.add(field);
        System.out.println(TAG + "add new Field:" + field.getName());
        /*throw new RuntimeException("can,t add new Field:" + field.getName()
                + "(" + field.getType() + "), " + "in class :"
                + field.getDefiningClass());*/
    }

    public Set<DexBackedField> getModifiedFields() {
        return this.modifiedFields;
    }

    public void addModifiedFields(DexBackedField field) {
        this.modifiedFields.add(field);
        System.out.println(TAG + "add modified field:" + field.getName());
        /*throw new RuntimeException("can,t modified Field:" + field.getName()
                + "(" + field.getType() + "), " + "in class :"
                + field.getDefiningClass());*/
    }

    public Set<DexBackedMethod> getAddedMethods() {
        return this.addedMethods;
    }

    public void addAddedMethods(DexBackedMethod method) {
        this.addedMethods.add(method);
        System.out.println("add new Method:" + method.getReturnType() + "  " + method.getName()
                + "(" + Formater.formatStringList(method.getParameterTypes())
                + ")  in Class:" + method.getDefiningClass());

        if (!this.modifiedClasses.contains(method.classDef)) {
            this.modifiedClasses.add(method.classDef);
        }
    }

    public Set<DexBackedMethod> getModifiedMethods() {
        return this.modifiedMethods;
    }

    public void addModifiedMethods(DexBackedMethod method) {
        System.out.println("add modified Method:" + method.getReturnType() + "  " + method.getName()
                + "(" + Formater.formatStringList(method.getParameterTypes())
                + ")  in Class:" + method.getDefiningClass());

        this.modifiedMethods.add(method);

        if (!this.modifiedClasses.contains(method.classDef)) {
            this.modifiedClasses.add(method.classDef);
        }
    }
}
