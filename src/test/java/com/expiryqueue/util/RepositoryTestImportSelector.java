package com.expiryqueue.util;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class RepositoryTestImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(final AnnotationMetadata annotationMetadata) {
        final AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                annotationMetadata.getAnnotationAttributes(RepositoryTest.class.getName(), true));
        return attributes.getStringArray("value");
    }
}