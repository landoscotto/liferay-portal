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

package com.liferay.taglib.ui;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.servlet.taglib.ui.LanguageEntry;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.taglib.util.IncludeTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Brian Wing Shun Chan
 */
public class LanguageTag extends IncludeTag {

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	public static final int LIST_ICON = 0;

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	public static final int LIST_LONG_TEXT = 1;

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	public static final int LIST_SHORT_TEXT = 2;

	/**
	 * @deprecated As of 7.0.0
	 */
	@Deprecated
	public static final int SELECT_BOX = 3;

	public void setDisplayCurrentLocale(boolean displayCurrentLocale) {
		_displayCurrentLocale = displayCurrentLocale;
	}

	/**
	 * @deprecated As of 7.0.0, replaced by {@link #setDisplayStyle(String)}.
	 */
	@Deprecated
	public void setDisplayStyle(int displayStyle) {
		_displayStyle = String.valueOf(displayStyle);
	}

	public void setDisplayStyle(String displayStyle) {
		_displayStyle = displayStyle;
	}

	public void setFormAction(String formAction) {
		_formAction = formAction;
	}

	public void setFormName(String formName) {
		_formName = formName;
	}

	public void setLanguageId(String languageId) {
		_languageId = languageId;
	}

	public void setLanguageIds(String[] languageIds) {
		_languageIds = languageIds;
	}

	public void setName(String name) {
		_name = name;
	}

	@Override
	protected void cleanUp() {
		_displayCurrentLocale = true;
		_displayStyle = _DISPLAY_STYLE;
		_formAction = null;
		_formName = "fm";
		_languageId = null;
		_languageIds = null;
		_name = "languageId";
	}

	protected List<LanguageEntry> getLanguageEntries(
		Locale[] locales, Locale currentLocale, boolean displayCurrentLocale,
		String formAction, String parameterName) {

		Map langCounts = new HashMap();

		for (Locale locale : locales) {
			Integer count = (Integer)langCounts.get(locale.getLanguage());

			if (count == null) {
				count = new Integer(1);
			}
			else {
				count = new Integer(count.intValue() + 1);
			}

			langCounts.put(locale.getLanguage(), count);
		}

		Set<String> duplicateLanguages = new HashSet<>();

		for (Locale locale : locales) {
			Integer count = (Integer)langCounts.get(locale.getLanguage());

			if (count.intValue() != 1) {
				duplicateLanguages.add(locale.getLanguage());
			}
		}

		List<LanguageEntry> languageEntries = new ArrayList<>();

		for (Locale locale : locales) {
			String url = null;

			if (!LocaleUtil.equals(locale, currentLocale)) {
				url = HttpUtil.addParameter(
					formAction, parameterName, LocaleUtil.toLanguageId(locale));
			}
			else if (!displayCurrentLocale) {
				continue;
			}

			languageEntries.add(
				new LanguageEntry(
					duplicateLanguages, currentLocale, locale, url));
		}

		return languageEntries;
	}

	@Override
	protected String getPage() {
		return _PAGE;
	}

	@Override
	protected void setAttributes(HttpServletRequest request) {
		request.setAttribute(
			"liferay-ui:language:displayCurrentLocale",
			String.valueOf(_displayCurrentLocale));

		String displayStyle = _displayStyle;

		if (!ArrayUtil.contains(_DISPLAY_STYLE_OPTIONS, displayStyle)) {
			displayStyle = _DISPLAY_STYLE_OPTIONS[0];
		}

		request.setAttribute(
			"liferay-ui:language:displayStyle", String.valueOf(displayStyle));

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		String formAction = _formAction;

		if (Validator.isNull(formAction)) {
			formAction = themeDisplay.getPathMain() +
				"/portal/update_language?p_l_id=" + themeDisplay.getPlid();

			formAction = HttpUtil.setParameter(
				formAction, "redirect", PortalUtil.getCurrentURL(request));
		}

		request.setAttribute("liferay-ui:language:formAction", formAction);
		request.setAttribute("liferay-ui:language:formName", _formName);
		request.setAttribute("liferay-ui:language:languageId", _languageId);

		Locale[] locales = null;

		if (ArrayUtil.isEmpty(_languageIds)) {
			locales = LanguageUtil.getAvailableLocales(
				themeDisplay.getSiteGroupId());
		}
		else {
			locales = LocaleUtil.fromLanguageIds(_languageIds);
		}

		request.setAttribute("liferay-ui:language:locales", locales);
		request.setAttribute("liferay-ui:language:name", _name);

		request.setAttribute(
			"liferay-ui:language:languageEntries",
			getLanguageEntries(
				locales, themeDisplay.getLocale(), _displayCurrentLocale,
				formAction, _name));
	}

	private static final String _DISPLAY_STYLE = GetterUtil.getString(
		PropsUtil.get(PropsKeys.LANGUAGE_DISPLAY_STYLE_DEFAULT));

	private static final String[] _DISPLAY_STYLE_OPTIONS = PropsUtil.getArray(
		PropsKeys.LANGUAGE_DISPLAY_STYLE_OPTIONS);

	private static final String _PAGE = "/html/taglib/ui/language/page.jsp";

	private boolean _displayCurrentLocale = true;
	private String _displayStyle = _DISPLAY_STYLE;
	private String _formAction;
	private String _formName = "fm";
	private String _languageId;
	private String[] _languageIds;
	private String _name = "languageId";

}