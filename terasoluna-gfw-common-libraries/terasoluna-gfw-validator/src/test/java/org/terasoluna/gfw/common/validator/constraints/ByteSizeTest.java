/*
 * Copyright(c) 2013 NTT DATA Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.terasoluna.gfw.common.validator.constraints;

import static java.util.Comparator.comparing;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.UnexpectedTypeException;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.terasoluna.gfw.common.validator.constraints.ByteSizeTest.ByteSizeTestForm;

/**
 * Test class of {@link ByteSize}
 */
public class ByteSizeTest extends AbstractConstraintsTest<ByteSizeTestForm> {

    private static final String MESSAGE_VALIDATION_ERROR = "must be between %d and %d bytes";

    @Before
    public void before() {
        form = new ByteSizeTestForm();
    }

    /**
     * input null value. expected valid.
     */
    @Test
    public void testInputNull() {

        violations = validator.validate(form);
        assertThat(violations.size(), is(0));
    }

    /**
     * specify min and max value. expected valid if input value encoded in UTF-8 is between min and max value.
     */
    @Test
    public void testSpecifyMinAndMaxValue() {

        {
            form.setStringProperty("aa");

            violations = validator.validate(form);
            assertThat(violations.size(), is(1));
            assertThat(violations.iterator().next().getMessage(), is(String
                    .format(MESSAGE_VALIDATION_ERROR, 3, 6)));
        }

        {
            form.setStringProperty("あ");

            violations = validator.validate(form);
            assertThat(violations.size(), is(0));
        }

        {
            form.setStringProperty("ああ");

            violations = validator.validate(form);
            assertThat(violations.size(), is(0));
        }

        {
            form.setStringProperty("ああa");

            violations = validator.validate(form);
            assertThat(violations.size(), is(1));
            assertThat(violations.iterator().next().getMessage(), is(String
                    .format(MESSAGE_VALIDATION_ERROR, 3, 6)));
        }
    }

    /**
     * specify max value for StringBuilder(CharSequence).
     */
    @Test
    public void testSpecifyMinAndMaxValueForStringBuilder() {

        {
            form.setStringBuilderProperty(new StringBuilder("aa"));

            violations = validator.validate(form);
            assertThat(violations.size(), is(1));
            assertThat(violations.iterator().next().getMessage(), is(String
                    .format(MESSAGE_VALIDATION_ERROR, 3, 6)));
        }

        {
            form.setStringBuilderProperty(new StringBuilder("あ"));

            violations = validator.validate(form);
            assertThat(violations.size(), is(0));
        }

        {
            form.setStringBuilderProperty(new StringBuilder("ああ"));

            violations = validator.validate(form);
            assertThat(violations.size(), is(0));
        }

        {
            form.setStringBuilderProperty(new StringBuilder("ああa"));

            violations = validator.validate(form);
            assertThat(violations.size(), is(1));
            assertThat(violations.iterator().next().getMessage(), is(String
                    .format(MESSAGE_VALIDATION_ERROR, 3, 6)));
        }
    }

    /**
     * specify charset. expected valid if input value encoded in specified charset is between min and max value.
     */
    @Test
    public void testSpecifyCharset() {

        {
            form.setStringProperty("あ");

            violations = validator.validate(form, SpecifyCharset.class);
            assertThat(violations.size(), is(1));
            assertThat(violations.iterator().next().getMessage(), is(String
                    .format(MESSAGE_VALIDATION_ERROR, 3, 6)));
        }

        {
            form.setStringProperty("あa");

            violations = validator.validate(form, SpecifyCharset.class);
            assertThat(violations.size(), is(0));
        }

        {
            form.setStringProperty("あああ");

            violations = validator.validate(form, SpecifyCharset.class);
            assertThat(violations.size(), is(0));
        }

        {
            form.setStringProperty("あああa");

            violations = validator.validate(form, SpecifyCharset.class);
            assertThat(violations.size(), is(1));
            assertThat(violations.iterator().next().getMessage(), is(String
                    .format(MESSAGE_VALIDATION_ERROR, 3, 6)));
        }
    }

    /**
     * specify illegal charset. expected {@code ValidationException} caused by {@code IllegalArgumentException} that message is
     * {@code failed to initialize validator by invalid argument}.
     */
    @Test
    public void testSpecifyIllegalCharset() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.validate(form, IllegalCharset.class));
        assertFailedToInitialize(ex, UnsupportedCharsetException.class);
    }

    /**
     * not specify min and max. expected valid if input value encoded in UTF-8 is between {@code 0} and {@link Long#MAX_VALUE} value.
     */
    @Test
    public void testSpecifyNotSpecifyMinAndMax() {

        {
            form.setStringProperty("");

            violations = validator.validate(form, NotSpecifyMinAndMax.class);
            assertThat(violations.size(), is(0));
        }

        {
            form.setStringProperty(String.format("%" + Long.MAX_VALUE + "d",
                    0));

            violations = validator.validate(form, NotSpecifyMinAndMax.class);
            assertThat(violations.size(), is(0));
        }
    }

    /**
     * specify negative min. expected {@code ValidationException} caused by {@code IllegalArgumentException} that message is
     * {@code failed to initialize validator by invalid argument} and nested by {@code IllegalArgumentException} that message is
     * {@code min[-1] must not be negative value.}.
     */
    @Test
    public void testSpecifyNegativeMin() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.validate(form, NegativeMin.class));
        assertFailedToInitialize(ex, IllegalArgumentException.class,
                "min[-1] must not be negative value.");
    }

    /**
     * specify negative max. expected {@code ValidationException} caused by {@code IllegalArgumentException} that message is
     * {@code failed to initialize validator by invalid argument} and nested by {@code IllegalArgumentException} that message is
     * {@code max[-1] must not be negative value.}.
     */
    @Test
    public void testNotSpecifyMax() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.validate(form, NegativeMax.class));
        assertFailedToInitialize(ex, IllegalArgumentException.class,
                "max[-1] must not be negative value.");
    }

    /**
     * specify same min and max value. expected valid if input value encoded in UTF-8 is equals to min and max value.
     */
    @Test
    public void testSpecifyMaxEqualsToMinValue() {

        {
            form.setStringProperty("aa");

            violations = validator.validate(form, MaxEqualsToMin.class);
            assertThat(violations.size(), is(1));
            assertThat(violations.iterator().next().getMessage(), is(String
                    .format(MESSAGE_VALIDATION_ERROR, 3, 3)));
        }

        {
            form.setStringProperty("あ");

            violations = validator.validate(form, MaxEqualsToMin.class);
            assertThat(violations.size(), is(0));
        }

        {
            form.setStringProperty("あa");

            violations = validator.validate(form, MaxEqualsToMin.class);
            assertThat(violations.size(), is(1));
            assertThat(violations.iterator().next().getMessage(), is(String
                    .format(MESSAGE_VALIDATION_ERROR, 3, 3)));
        }
    }

    /**
     * specify max lower than min value. expected {@code ValidationException} caused by {@code IllegalArgumentException} that message is
     * {@code failed to initialize validator by invalid argument} and nested by {@code IllegalArgumentException} that message is
     * {@code max[2] must be higher or equal to min[3].}.
     */
    @Test
    public void testAnnotateMaxLowerThanMinValue() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.validate(form, MaxLowerThanMin.class));
        assertFailedToInitialize(ex, IllegalArgumentException.class,
                "max[2] must be higher or equal to min[3].");
    }

    /**
     * specify not support type. expected {@code UnexpectedTypeException}
     */
    @Test
    public void testAnnotateUnexpectedType() {
        assertThrows(UnexpectedTypeException.class, () -> validator.validate(
                form, UnexpectedType.class));
    }

    /**
     * all values in the collection are valid.
     */
    @Test
    public void testCollectionValid() {
        form.setListProperty(Arrays.asList("あ", "ああ"));
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<ByteSizeTestForm>> violations = validator
                .validate(form);

        assertThat(violations, is(notNullValue()));
        assertThat(violations.size(), is(0));
    }

    /**
     * first value in the collection is invalid.
     */
    @Test
    public void testCollectionFirstInvalid() {
        form.setListProperty(Arrays.asList("aa", "あ"));
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<ByteSizeTestForm>> violations = validator
                .validate(form);

        assertThat(violations, is(notNullValue()));
        assertThat(violations.size(), is(1));

        ConstraintViolation<ByteSizeTestForm> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is(
                "listProperty[0].<list element>"));
        assertThat(v.getMessage(), is(String.format(MESSAGE_VALIDATION_ERROR, 3,
                6)));
    }

    /**
     * last value in the collection is invalid.
     */
    @Test
    public void testCollectionLastInvalid() {
        form.setListProperty(Arrays.asList("ああ", "ああa"));
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<ByteSizeTestForm>> violations = validator
                .validate(form);

        assertThat(violations, is(notNullValue()));
        assertThat(violations.size(), is(1));

        ConstraintViolation<ByteSizeTestForm> v = violations.iterator().next();
        assertThat(v.getPropertyPath().toString(), is(
                "listProperty[1].<list element>"));
        assertThat(v.getMessage(), is(String.format(MESSAGE_VALIDATION_ERROR, 3,
                6)));
    }

    /**
     * all values in the collection are invalid.
     */
    @Test
    public void testCollectionAllInvalid() {
        form.setListProperty(Arrays.asList("aa", "ああa"));
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<ByteSizeTestForm>> violations = validator
                .validate(form);

        assertThat(violations, is(notNullValue()));
        assertThat(violations.size(), is(2));

        Iterator<ConstraintViolation<ByteSizeTestForm>> iterator = violations
                .stream().sorted(comparing(v -> v.getPropertyPath().toString()))
                .iterator();

        ConstraintViolation<ByteSizeTestForm> violation = iterator.next();
        assertThat(violation.getPropertyPath().toString(), is(
                "listProperty[0].<list element>"));
        assertThat(violation.getMessage(), is(String.format(
                MESSAGE_VALIDATION_ERROR, 3, 6)));
        violation = iterator.next();
        assertThat(violation.getPropertyPath().toString(), is(
                "listProperty[1].<list element>"));
        assertThat(violation.getMessage(), is(String.format(
                MESSAGE_VALIDATION_ERROR, 3, 6)));
    }

    /**
     * Validation group encoding shift-jis.
     */
    private static interface SpecifyCharset {
    };

    /**
     * Validation group encoding unsupported.
     */
    private static interface IllegalCharset {
    };

    /**
     * Validation group unexpected type.
     */
    private static interface UnexpectedType {
    };

    /**
     * Validation group min and max are not specified.
     */
    private static interface NotSpecifyMinAndMax {
    };

    /**
     * Validation group min negative.
     */
    private static interface NegativeMin {
    };

    /**
     * Validation group max negative.
     */
    private static interface NegativeMax {
    };

    /**
     * Validation group max equals to min.
     */
    private static interface MaxEqualsToMin {
    };

    /**
     * Validation group max lower than min.
     */
    private static interface MaxLowerThanMin {
    };

    public class ByteSizeTestForm {
        @ByteSize(min = 3, max = 6)
        @ByteSize(min = 3, max = 6, charset = "shift-jis", groups = {
                SpecifyCharset.class })
        @ByteSize(min = 3, max = 6, charset = "illegal-charset", groups = {
                IllegalCharset.class })
        @ByteSize(groups = { NotSpecifyMinAndMax.class })
        @ByteSize(min = -1, groups = { NegativeMin.class })
        @ByteSize(max = -1, groups = { NegativeMax.class })
        @ByteSize(min = 3, max = 3, groups = { MaxEqualsToMin.class })
        @ByteSize(min = 3, max = 2, groups = { MaxLowerThanMin.class })
        private String stringProperty;

        @ByteSize(min = 3, max = 6)
        private StringBuilder stringBuilderProperty;

        @ByteSize(min = 3, max = 6, groups = { UnexpectedType.class })
        private Integer intProperty;

        private List<@ByteSize(min = 3, max = 6) String> listProperty;

        public String getStringProperty() {
            return stringProperty;
        }

        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }

        public StringBuilder getStringBuilderProperty() {
            return stringBuilderProperty;
        }

        public void setStringBuilderProperty(
                StringBuilder stringBuilderProperty) {
            this.stringBuilderProperty = stringBuilderProperty;
        }

        public Integer getIntProperty() {
            return intProperty;
        }

        public void setIntProperty(Integer intProperty) {
            this.intProperty = intProperty;
        }

        public List<String> getListProperty() {
            return listProperty;
        }

        public void setListProperty(List<String> listProperty) {
            this.listProperty = listProperty;
        }
    }
}
