/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.util;

import com.liferay.dynamic.data.mapping.BaseDDMTestCase;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutColumn;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutPage;
import com.liferay.dynamic.data.mapping.model.DDMFormLayoutRow;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil;
import com.liferay.dynamic.data.mapping.storage.Field;
import com.liferay.dynamic.data.mapping.storage.Fields;
import com.liferay.dynamic.data.mapping.util.impl.DDMImpl;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.io.Serializable;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;

/**
 * @author Marcellus Tavares
 */
@PrepareForTest({DDMStructureLocalServiceUtil.class, LocaleUtil.class})
@SuppressStaticInitializationFor(
	"com.liferay.dynamic.data.mapping.service.DDMStructureLocalServiceUtil"
)
public class DDMImplTest extends BaseDDMTestCase {

	@Before
	public void setUp() throws Exception {
		setUpConfigurationFactoryUtil();
		setUpDDMFormFieldTypeServicesTrackerUtil();
		setUpDDMFormJSONDeserializerUtil();
		setUpDDMFormJSONSerializerUtil();
		setUpDDMStructureLocalServiceUtil();
		setUpJSONFactoryUtil();
		setUpHtmlUtil();
		setUpLanguageUtil();
		setUpLocaleUtil();
		setUpPropsUtil();
		setUpSAXReaderUtil();
	}

	@Test
	public void testGetDefaultDDMFormLayout() {
		DDMForm ddmForm = createDDMForm("Text1", "Text2");

		DDMFormLayout ddmFormLayout = _ddmImpl.getDefaultDDMFormLayout(ddmForm);

		List<DDMFormLayoutPage> ddmFormLayoutPages =
			ddmFormLayout.getDDMFormLayoutPages();

		Assert.assertEquals(1, ddmFormLayoutPages.size());

		DDMFormLayoutPage ddmFormLayoutPage = ddmFormLayoutPages.get(0);

		List<DDMFormLayoutRow> ddmFormLayoutRows =
			ddmFormLayoutPage.getDDMFormLayoutRows();

		Assert.assertEquals(2, ddmFormLayoutRows.size());

		assertDefaultDDMFormLayoutRow("Text1", ddmFormLayoutRows.get(0));
		assertDefaultDDMFormLayoutRow("Text2", ddmFormLayoutRows.get(1));
	}

	@Test
	public void testMergeFieldsAfterFieldValueIsRemovedFromTheMiddleOfSeries()
		throws Exception {

		DDMForm ddmForm = createDDMForm();

		addDDMFormFields(
			ddmForm, createTextDDMFormField("Content", "", true, true, false));

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field existingContentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content 1", "Content 2", "Content 3"),
			createValuesList("Conteudo 1", "Conteudo 2 ", "Conteudo 3"));

		Field existingFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Content_INSTANCE_ovho,Content_INSTANCE_zuvh," +
			"Content_INSTANCE_yiek");

		Fields existingFields = createFields(
			existingContentField, existingFieldsDisplayField);

		Field newContentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content 1", "Content 3"), null);

		Field newFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Content_INSTANCE_ovho,Content_INSTANCE_yiek");

		Fields newFields = createFields(newContentField, newFieldsDisplayField);

		Fields actualFields = _ddmImpl.mergeFields(newFields, existingFields);

		Field actualContentField = actualFields.get("Content");

		Assert.assertNotNull(actualContentField);

		testValues(
			actualContentField.getValues(LocaleUtil.US), "Content 1",
			"Content 3");
		testValues(
			actualContentField.getValues(LocaleUtil.BRAZIL), "Conteudo 1",
			"Conteudo 3");
	}

	@Test
	public void testMergeFieldsAfterNewFieldIsAdded() throws Exception {
		DDMForm ddmForm = createDDMForm();

		addDDMFormFields(
			ddmForm, createTextDDMFormField("Title"),
			createTextDDMFormField("Content"));

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field existingTitleField = createField(
			ddmStructure.getStructureId(), "Title",
			createValuesList("Title value"), null);

		Field existingFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(), "Title_INSTANCE_ovho");

		Fields existingFields = createFields(
			existingTitleField, existingFieldsDisplayField);

		Field newContentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content value"), null);

		Field newFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Title_INSTANCE_ovho,Content_INSTANCE_yiek");

		Fields newFields = createFields(
			existingTitleField, newContentField, newFieldsDisplayField);

		Fields actualFields = _ddmImpl.mergeFields(newFields, existingFields);

		Field actualContentField = actualFields.get("Content");

		Assert.assertNotNull(actualContentField);
		Assert.assertEquals(
			"Content value", actualContentField.getValue(LocaleUtil.US));
	}

	@Test
	public void testMergeFieldsAfterNewFieldValueIsInsertedInTheMiddleOfSeries()
		throws Exception {

		DDMForm ddmForm = createDDMForm();

		addDDMFormFields(
			ddmForm, createTextDDMFormField("Content", "", true, true, false));

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field existingContentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content 1", "Content 3"),
			createValuesList("Conteudo 1", "Conteudo 3"));

		Field existingFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Content_INSTANCE_ovho,Content_INSTANCE_yiek");

		Fields existingFields = createFields(
			existingContentField, existingFieldsDisplayField);

		Field newContentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content 1", "Content 2", "Content 3"), null);

		Field newFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Content_INSTANCE_ovho,Content_INSTANCE_zuvh," +
			"Content_INSTANCE_yiek");

		Fields newFields = createFields(newContentField, newFieldsDisplayField);

		Fields actualFields = _ddmImpl.mergeFields(newFields, existingFields);

		Field actualContentField = actualFields.get("Content");

		Assert.assertNotNull(actualContentField);

		testValues(
			actualContentField.getValues(LocaleUtil.US), "Content 1",
			"Content 2", "Content 3");
		testValues(
			actualContentField.getValues(LocaleUtil.BRAZIL), "Conteudo 1",
			"Content 2", "Conteudo 3");
	}

	@Test
	public void testMergeFieldsAfterNewLocalizedFieldValueIsAdded()
		throws Exception {

		DDMForm ddmForm = createDDMForm();

		addDDMFormFields(ddmForm, createTextDDMFormField("Title"));

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field existingTitleField = createField(
			ddmStructure.getStructureId(), "Title",
			createValuesList("Title value"), null);

		Field existingFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(), "Title_INSTANCE_ovho");

		Fields existingFields = createFields(
			existingTitleField, existingFieldsDisplayField);

		Field newTitleField = createField(
			ddmStructure.getStructureId(), "Title",
			createValuesList("Modified title value"),
			createValuesList("Valor do titulo modificado"));

		Field newFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(), "Title_INSTANCE_ovho");

		Fields newFields = createFields(newTitleField, newFieldsDisplayField);

		Fields actualFields = _ddmImpl.mergeFields(newFields, existingFields);

		Field actualContentField = actualFields.get("Title");

		Assert.assertNotNull(actualContentField);
		Assert.assertEquals(
			"Modified title value", actualContentField.getValue(LocaleUtil.US));
		Assert.assertEquals(
			"Valor do titulo modificado",
			actualContentField.getValue(LocaleUtil.BRAZIL));
	}

	@Test
	public void testMergeFieldsAfterRepeatableParentAndChildAreModified()
		throws Exception {

		DDMForm ddmForm = createDDMForm();

		DDMFormField textDDMFormField = createTextDDMFormField(
			"Name", "", true, true, false);

		List<DDMFormField> nestedDDMFormFields =
			textDDMFormField.getNestedDDMFormFields();

		nestedDDMFormFields.add(
			createTextDDMFormField("Phone", "", true, true, false));

		addDDMFormFields(ddmForm, textDDMFormField);

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field existingNameField = createField(
			ddmStructure.getStructureId(), "Name",
			createValuesList("Paul", "Joe"),
			createValuesList("Paulo", "Joao"));

		Field existingPhoneField = createField(
			ddmStructure.getStructureId(), "Phone",
			createValuesList("Paul's Phone 1", "Paul's Phone 2", "Joe's Phone"),
			createValuesList(
				"Telefone de Paulo 1", "Telefone de Paulo 2",
				"Telefone de Joao"));

		Field existingFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Name_INSTANCE_rztm,Phone_INSTANCE_ovho,Phone_INSTANCE_krvx," +
			"Name_INSTANCE_rght,Phone_INSTANCE_latb");

		Fields existingFields = createFields(
			existingNameField, existingPhoneField, existingFieldsDisplayField);

		Field newNameField = createField(
			ddmStructure.getStructureId(), "Name",
			createValuesList("Paul Smith", "Joe William", "Charlie Parker"),
			null);

		Field newPhoneField = createField(
			ddmStructure.getStructureId(), "Phone",
			createValuesList(
				"Paul Smith phone", "Joe William Phone 1",
				"Joe William Phone 2", "Charlie Parker phone"),
			null);

		Field newFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Name_INSTANCE_rztm,Phone_INSTANCE_ovho,Name_INSTANCE_rght," +
			"Phone_INSTANCE_latb,Phone_INSTANCE_uytw,Name_INSTANCE_jwop," +
			"Phone_INSTANCE_yhgl");

		Fields newFields = createFields(
			newNameField, newPhoneField, newFieldsDisplayField);

		Fields actualFields = _ddmImpl.mergeFields(newFields, existingFields);

		Field actualNameField = actualFields.get("Name");

		Assert.assertNotNull(actualNameField);

		testValues(
			actualNameField.getValues(LocaleUtil.US), "Paul Smith",
			"Joe William", "Charlie Parker");
		testValues(
			actualNameField.getValues(LocaleUtil.BRAZIL), "Paulo", "Joao",
			"Charlie Parker");

		Field actualPhoneField = actualFields.get("Phone");

		Assert.assertNotNull(actualPhoneField);

		testValues(
			actualPhoneField.getValues(LocaleUtil.US), "Paul Smith phone",
			"Joe William Phone 1", "Joe William Phone 2",
			"Charlie Parker phone");
		testValues(
			actualPhoneField.getValues(LocaleUtil.BRAZIL),
			"Telefone de Paulo 1", "Telefone de Joao", "Joe William Phone 2",
			"Charlie Parker phone");
	}

	@Test
	public void testMergeFieldsAfterRepeatableTransientParentIsAppended()
		throws Exception {

		DDMForm ddmForm = createDDMForm();

		DDMFormField separatorDDMFormField = createSeparatorDDMFormField(
			"Separator", true);

		addNestedTextDDMFormFields(separatorDDMFormField, "Content");

		addDDMFormFields(ddmForm, separatorDDMFormField);

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field existingContentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content 1", "Content 2"),
			createValuesList("Conteudo 1", "Conteudo 2"));

		Field existingFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Separator_INSTANCE_rztm,Content_INSTANCE_ovho," +
			"Separator_INSTANCE_krvx,Content_INSTANCE_yiek");

		Fields existingFields = createFields(
			existingContentField, existingFieldsDisplayField);

		Field newContentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content 1", "Content 2", "Content 3"), null);

		Field newFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Separator_INSTANCE_rztm,Content_INSTANCE_ovho," +
			"Separator_INSTANCE_krvx,Content_INSTANCE_yiek," +
			"Separator_INSTANCE_yhrw,Content_INSTANCE_jtvx");

		Fields newFields = createFields(newContentField, newFieldsDisplayField);

		Fields actualFields = _ddmImpl.mergeFields(newFields, existingFields);

		Field actualContentField = actualFields.get("Content");

		Assert.assertNotNull(actualContentField);

		testValues(
			actualContentField.getValues(LocaleUtil.US), "Content 1",
			"Content 2", "Content 3");
		testValues(
			actualContentField.getValues(LocaleUtil.BRAZIL), "Conteudo 1",
			"Conteudo 2", "Content 3");
	}

	@Test
	public void testMergeFieldsWithFieldsValuesWithNoInstanceSuffix()
		throws Exception {

		DDMForm ddmForm = createDDMForm();

		addDDMFormFields(
			ddmForm, createTextDDMFormField("Content", "", true, true, false));

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field existingContentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content 1", "Content 2", "Content 3"),
			createValuesList("Conteudo 1", "Conteudo 2 ", "Conteudo 3"));

		Field existingFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(), "Content,Content,Content");

		Fields existingFields = createFields(
			existingContentField, existingFieldsDisplayField);

		Field newContentField = createField(
			ddmStructure.getStructureId(), "Content",
			createValuesList("Content 1", "Content 3"), null);

		Field newFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(), "Content,Content");

		Fields newFields = createFields(newContentField, newFieldsDisplayField);

		try {
			_ddmImpl.mergeFields(newFields, existingFields);

			Assert.fail();
		}
		catch (NullPointerException npe) {
		}
	}

	@Test
	public void testMergeFieldsWithMatchingFieldNames() throws Exception {
		DDMForm ddmForm = createDDMForm();

		DDMFormField textDDMFormField = createTextDDMFormField(
			"Name", "", true, true, false);

		List<DDMFormField> nestedDDMFormFields =
			textDDMFormField.getNestedDDMFormFields();

		nestedDDMFormFields.add(
			createTextDDMFormField("NameNested", "", true, true, false));

		addDDMFormFields(ddmForm, textDDMFormField);

		DDMStructure ddmStructure = createStructure("Test Structure", ddmForm);

		Field existingNameField = createField(
			ddmStructure.getStructureId(), "Name",
			createValuesList("Name 1", "Name 2"),
			createValuesList("Nome 1", "Nome 2"));

		Field existingNameNestedField = createField(
			ddmStructure.getStructureId(), "NameNested",
			createValuesList("Name nested 1", "Name nested 2"),
			createValuesList("Nome nested 1", "Nome nested 2"));

		Field existingFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Name_INSTANCE_rztm,NameNested_INSTANCE_ovho,Name_INSTANCE_krvx," +
			"NameNested_INSTANCE_rght");

		Fields existingFields = createFields(
			existingNameField, existingNameNestedField,
			existingFieldsDisplayField);

		Field newNameField = createField(
			ddmStructure.getStructureId(), "Name", null, null);

		Field newNameNestedField = createField(
			ddmStructure.getStructureId(), "NameNested", null, null);

		Field newFieldsDisplayField = createFieldsDisplayField(
			ddmStructure.getStructureId(),
			"Name_INSTANCE_rztm,NameNested_INSTANCE_ovho,Name_INSTANCE_krvx," +
			"NameNested_INSTANCE_rght");

		Fields newFields = createFields(
			newNameField, newNameNestedField, newFieldsDisplayField);

		Fields actualFields = _ddmImpl.mergeFields(newFields, existingFields);

		Field actualNameField = actualFields.get("Name");

		testValues(
			actualNameField.getValues(LocaleUtil.US), "Name 1", "Name 2");
		testValues(
			actualNameField.getValues(LocaleUtil.BRAZIL), "Nome 1", "Nome 2");

		Field actualNameNestedField = actualFields.get("NameNested");

		testValues(
			actualNameNestedField.getValues(LocaleUtil.US), "Name nested 1",
			"Name nested 2");
		testValues(
			actualNameNestedField.getValues(LocaleUtil.BRAZIL), "Nome nested 1",
			"Nome nested 2");
	}

	protected void assertDefaultDDMFormLayoutRow(
		String expectedDDMFormFieldName,
		DDMFormLayoutRow actualDDMFormLayoutRow) {

		List<DDMFormLayoutColumn> actualDDMFormLayoutColumns =
			actualDDMFormLayoutRow.getDDMFormLayoutColumns();

		Assert.assertEquals(1, actualDDMFormLayoutColumns.size());

		DDMFormLayoutColumn actualDDMFormLayoutColumn =
			actualDDMFormLayoutColumns.get(0);

		List<String> actualDDMFormFieldNames =
			actualDDMFormLayoutColumn.getDDMFormFieldNames();

		Assert.assertEquals(
			expectedDDMFormFieldName, actualDDMFormFieldNames.get(0));
		Assert.assertEquals(
			DDMFormLayoutColumn.FULL, actualDDMFormLayoutColumn.getSize());
	}

	protected void testValues(
		List<Serializable> actualValues, String... expectedValues) {

		Assert.assertEquals(expectedValues.length, actualValues.size());

		for (int i = 0; i < expectedValues.length; i++) {
			Assert.assertEquals(expectedValues[i], actualValues.get(i));
		}
	}

	private final DDMImpl _ddmImpl = new DDMImpl();

}