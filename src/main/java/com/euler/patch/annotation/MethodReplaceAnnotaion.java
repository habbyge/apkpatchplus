//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.euler.patch.annotation;

import java.util.HashSet;
import java.util.Set;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.base.BaseAnnotation;
import org.jf.dexlib2.base.BaseAnnotationElement;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue;

public class MethodReplaceAnnotaion extends BaseAnnotation {
    private static final String ANNOTATION = "Lcom/habbyge/iwatch/patch/FixMethodAnno;";
    private final Set<BaseAnnotationElement> mElements = new HashSet<>();

    public int getVisibility() {
        return AnnotationVisibility.getVisibility("runtime");
    }

    // 新增的类不加 "_CF" 后缀，避免找不到
    public String getType() {
        return ANNOTATION;
    }

    public MethodReplaceAnnotaion(final String clazz, final String method) {
        BaseAnnotationElement clazzElement = new BaseAnnotationElement() {

            @Override
            public EncodedValue getValue() {
                String name = clazz.substring(1, clazz.length() - 1).replace('/', '.');
                return new ImmutableStringEncodedValue(name);
            }

            @Override
            public String getName() {
                return "clazz";
            }
        };
        this.mElements.add(clazzElement);
        BaseAnnotationElement methodElement = new BaseAnnotationElement() {

            @Override
            public EncodedValue getValue() {
                return new ImmutableStringEncodedValue(method);
            }

            @Override
            public String getName() {
                return "method";
            }
        };
        this.mElements.add(methodElement);
    }

    public Set<? extends AnnotationElement> getElements() {
        return this.mElements;
    }
}
