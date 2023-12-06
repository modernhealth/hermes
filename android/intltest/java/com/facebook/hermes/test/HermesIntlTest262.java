/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.facebook.hermes.test;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.app.Instrumentation;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.hermes.intltest.BuildConfig;
import com.facebook.hermes.test.JSRuntime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.runners.Parameterized;


// Run "./gradlew :intltest:prepareTests" from the root to copy the test files to the
// APK assets.
@RunWith(Parameterized.class)
public final class HermesIntlTest262 {
  private static final String LOG_TAG = "HermesIntlTest";

  protected static String loadFileContentsFromAsset(String filename) throws IOException {
    Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    AssetManager assets = instrumentation.getContext().getAssets();
    InputStream is = assets.open(filename);

    BufferedReader r = new BufferedReader(new InputStreamReader(is));
    StringBuilder total = new StringBuilder();
    for (String line; (line = r.readLine()) != null; ) {
      total.append(line).append('\n');
    }

    return total.toString();
  }

  protected static void evalScriptFromAsset(JSRuntime rt, String filename) throws IOException {
    String script = loadFileContentsFromAsset(filename);

    rt.evaluateJavaScript(script);
  }

  protected static Set<String> unsupportedFeatures = new HashSet<String>(Arrays.asList(
      "Temporal",
      "Intl.NumberFormat-v3"
  ));

  protected List<String> getFeaturesList() {
    // Parse a string like similar to "features: [class, class-fields-public, arrow-function]"
    Pattern pattern = Pattern.compile("features:\\s*\\[(.*)?\\]");
    Matcher matcher = pattern.matcher(this.contents);

    if (!matcher.find()) {
      return Collections.emptyList();
    }

    String featureList = matcher.group(1);
    String[] features = featureList.split("[ ]*,[ ]*");

    return Arrays.asList(features);
  }

  protected boolean hasUnsupportedFeatures() {
    List<String> features = getFeaturesList();

    for (String feature : features) {
      Log.d(LOG_TAG, "Feature " + feature);
      if (unsupportedFeatures.contains(feature)) {
        return true;
      }
    }

     return false;
  }

  protected static void evaluateCommonScriptsFromAsset(JSRuntime rt) throws IOException {
    evalScriptFromAsset(rt, "test262/harness/sta.js");
    evalScriptFromAsset(rt, "test262/harness/assert.js");
    evalScriptFromAsset(rt, "test262/harness/testIntl.js");
    evalScriptFromAsset(rt, "test262/harness/propertyHelper.js");
    evalScriptFromAsset(rt, "test262/harness/compareArray.js");
    evalScriptFromAsset(rt, "test262/harness/dateConstants.js");
    evalScriptFromAsset(rt, "test262/harness/isConstructor.js");
    evalScriptFromAsset(rt, "test262/harness/testTypedArray.js");
  }

  @Parameterized.Parameters(name = BuildConfig.TEST_BASE_DIR + "/{0}")
  public static Iterable<? extends Object> data() {
    try {
      return findAllTestCasesRuntime();
    } catch (IOException exc) {
      System.out.println(exc.getMessage());
      return null;
    }
  }

  private final String path;

  public HermesIntlTest262(String path) {
    this.path = path;

  }

  private String contents;

  @Before
  public void setUp() throws IOException {
    this.contents = loadFileContentsFromAsset(this.path);

    Assume.assumeTrue(!hasUnsupportedFeatures());
  }

  @Test
  public void test262Intl() {
    try {
      Log.d(LOG_TAG, "Evaluating " + path);

      try (JSRuntime rt = JSRuntime.makeHermesRuntime()) {
        evaluateCommonScriptsFromAsset(rt);

        try {
           rt.evaluateJavaScript(this.contents);

        } catch (com.facebook.jni.CppException ex) {
          Assert.fail(ex.getMessage());
        }
      }
    } catch (IOException exc) {
      Assert.fail(exc.getMessage());
    }
  }

  @After
  public void cleanUp() {
    this.contents = null;
  }

  public static List<String> findAllTestCasesRuntime() throws IOException {
    List<String> paths = new ArrayList<>();
    Set<String> skipList = getSkipList();
    Stack<String> testFiles = new Stack<>();
    testFiles.push("test262/test");
    AssetManager assets = InstrumentationRegistry.getInstrumentation().getContext().getAssets();
    ArrayList<String> ranTests = new ArrayList<>();
    HashMap<String, String> failedTests = new HashMap<>();

    while (!testFiles.isEmpty()) {
      String path = testFiles.pop();
      String[] contents = assets.list(path);
      if (skipList.contains(path)) continue;
      // If this is a subdirectory, add all of its contents to the list.
      if (contents.length > 0) {
        for (String filename : assets.list(path)) {
          testFiles.push(path + "/" + filename);
        }
        Log.v(LOG_TAG, "Found subdirectory " + path);
        continue;
      }

      paths.add(path);
    }

    return paths;
  }

  private static Set<String> getSkipList() {
    Set<String> skipList = new HashSet<>();

    // Intl.getCanonicalLocales
    skipList.addAll(
        Arrays.asList(
            "test262/test/intl402/Intl/getCanonicalLocales/Locale-object.js",
            // ICU4J adds an extra 'yes' token to the unicode 'ca' extension.
            "test262/test/intl402/Intl/getCanonicalLocales/canonicalized-tags.js",
            // ICU4J don't do complex region replacement.
            "test262/test/intl402/Intl/getCanonicalLocales/complex-region-subtag-replacement.js",
            // ICU4J adds an extra 'yes' token to the unicode 'kf' extension
            "test262/test/intl402/Intl/getCanonicalLocales/non-iana-canon.js",
            // ICU4J doesn't do variant replacement.
            "test262/test/intl402/Intl/getCanonicalLocales/preferred-variant.js",
            // ICU4J doesn't canonicalize extensions.
            "test262/test/intl402/Intl/getCanonicalLocales/transformed-ext-canonical.js",
            "test262/test/intl402/Intl/getCanonicalLocales/transformed-ext-invalid.js",
            "test262/test/intl402/Intl/getCanonicalLocales/unicode-ext-canonicalize-region.js",
            "test262/test/intl402/Intl/getCanonicalLocales/unicode-ext-canonicalize-subdivision.js",
            "test262/test/intl402/Intl/getCanonicalLocales/unicode-ext-canonicalize-yes-to-true.js",
            "test262/test/intl402/Intl/getCanonicalLocales/unicode-ext-key-with-digit.js",
            // ICU4J doesn't perform all grandfathered tag replacements.
            "test262/test/intl402/Intl/getCanonicalLocales/grandfathered.js",
            // ICU4J doesn't perform all grandfathered tag replacements.
            // Note:: CLDR has a list of grandfathered/language/script/region/variant
            // replacements that must happen along with the canonicalization.
            // (https://github.com/unicode-org/cldr/blob/master/common/supplemental/supplementalMetadata.xml)
            // But, typically, the implementation can't/shouldn't lookup cldr while
            // canonicaliation as it can be costly.
            // ICU typically hardcodes a small subset of translations in code .. for inst:
            // https://github.com/unicode-org/icu/blob/12dc3772b1858c73bedd5cffdee0a5a41ce7c61a/ICU4J/main/classes/core/src/com/ibm/icu/impl/locale/LanguageTag.java#L43
            // Which implies ICU APIs doesn't perform all translations based on spec.
            // Note that our canonicalization implementaton for pre-24 platform attemps to do
            // most of the translations, based on tables generated from CLDR.
            // Another thing to note is that different version of android platform ships with
            // different versions of ICU4J and CLDR. (
            // https://developer.android.com/guide/topics/resources/internationalization )
            // Which means, same locale id can potentially be localized to different canonical
            // locale ids.
            "test262/test/intl402/Intl/getCanonicalLocales/preferred-grandfathered.js",
            // ICU4J canonicalization doesn't reject all the locales that are invalid based on spec.
            "test262/test/intl402/Intl/getCanonicalLocales/invalid-tags.js",
            // ICU4J canonicalization doesn't perform complex subtag replacements.
            "test262/test/intl402/Intl/getCanonicalLocales/complex-language-subtag-replacement.js",
            // We currently don't call the "has" trap in proxy.
            "test262/test/intl402/Intl/getCanonicalLocales/has-property.js"));

    // Intl.Collator
    skipList.addAll(
        Arrays.asList(
            "test262/test/intl402/Collator/constructor-options-throwing-getters.js",
            "test262/test/intl402/Collator/subclassing.js",
            "test262/test/intl402/Collator/proto-from-ctor-realm.js",
            "test262/test/intl402/Collator/prototype/resolvedOptions/order.js",
            "test262/test/intl402/Collator/prototype/compare/ignorePunctuation.js"));

    // Intl.DateTimeFormat
    skipList.addAll(
        Arrays.asList(
            "test262/test/intl402/DateTimeFormat/taint-Object-prototype-date-time-components.js",
            "test262/test/intl402/DateTimeFormat/constructor-options-order.js",
            "test262/test/intl402/DateTimeFormat/constructor-options-order-timedate-style.js",
            "test262/test/intl402/DateTimeFormat/timezone-canonicalized.js",
            "test262/test/intl402/DateTimeFormat/timezone-utc.js",
            "test262/test/intl402/DateTimeFormat/constructor-options-order-fractionalSecondDigits.js",
            "test262/test/intl402/DateTimeFormat/constructor-options-order-dayPeriod.js",
            "test262/test/intl402/DateTimeFormat/constructor-options-timeZoneName-valid.js",
            "test262/test/intl402/DateTimeFormat/prototype/format/temporal-zoneddatetime-not-supported.js",
            "test262/test/intl402/DateTimeFormat/prototype/formatToParts/temporal-objects-resolved-time-zone.js",
            "test262/test/intl402/DateTimeFormat/prototype/format/temporal-objects-resolved-time-zone.js",
            "test262/test/Intl/intl402/DateTimeFormat/prototype/formatRange/fails-on-distinct-temporal-types.js",
            "test262/test/intl402/DateTimeFormat/prototype/formatRange/temporal-zoneddatetime-not-supported.js",
            "test262/test/intl402/DateTimeFormat/prototype/formatRange/temporal-zoneddatetime-not-supported.js",
            "test262/test/intl402/DateTimeFormat/prototype/formatRange/temporal-objects-resolved-time-zone.js",
            "test262/test/intl402/DateTimeFormat/prototype/formatToParts/temporal-zoneddatetime-not-supported.js",
            "test262/test/intl402/DateTimeFormat/prototype/formatRangeToParts/temporal-objects-resolved-time-zone.js",
            "test262/test/intl402/DateTimeFormat/prototype/formatRangeToParts/temporal-zoneddatetime-not-supported.js",
            "test262/test/Intl/intl402/DateTimeFormat/prototype/formatRangeToParts/fails-on-distinct-temporal-types.js",
            // "test262/test/intl402/DateTimeFormat/prototype/formatRangeToParts/date-same-returns-single-date.js",
            "test262/test/intl402/DateTimeFormat/subclassing.js",
            "test262/test/intl402/DateTimeFormat/proto-from-ctor-realm.js",
            // TODO: Investigate why this fails.
            "test262/test/intl402/DateTimeFormat/prototype/format/proleptic-gregorian-calendar.js",
            "test262/test/intl402/DateTimeFormat/prototype/resolvedOptions/order.js",
            "test262/test/intl402/DateTimeFormat/prototype/resolvedOptions/order-style.js",
            "test262/test/intl402/DateTimeFormat/prototype/resolvedOptions/order-fractionalSecondDigits.js",
            "test262/test/intl402/DateTimeFormat/prototype/resolvedOptions/hourCycle-dateStyle.js",
            "test262/test/intl402/DateTimeFormat/prototype/resolvedOptions/hourCycle-timeStyle.js",
            "test262/test/intl402/DateTimeFormat/prototype/resolvedOptions/order-dayPeriod.js",
            // Requires Intl.supportedValuesOf
            "test262/test/intl402/DateTimeFormat/timezone-case-insensitive.js"));

    // Intl.NumberFormat
    skipList.addAll(
        Arrays.asList(
            // We currently don't call the "has" trap in proxy.
            "test262/test/intl402/NumberFormat/constructor-locales-hasproperty.js",
            // This test is not correct based on spec. With ths currency in test, the default
            // minfractiondigits is 2, and the maxfractiondigita cannot be set to 1. Both Firefox
            // and Chrome also fail the test.
            "test262/test/intl402/NumberFormat/dft-currency-mnfd-range-check-mxfd.js",
            // When strictly following spec, the currency checks comes before unit check and the
            // test will throw RangeError on currency validation before reaching the cdoe which
            // throws TypeError on seeing undefined unit. But, we have a part of option validation
            // in C++ code which throws all TypeErrors, which results in the TypeError getting
            // thrown.
            "test262/test/intl402/NumberFormat/constructor-order.js",
            // Didn't get correct minimumFractionDigts for currency AFN.
            "test262/test/intl402/NumberFormat/currency-digits.js",
            // We support only units directly known to
            // https://developer.android.com/reference/android/icu/util/MeasureUnit
            "test262/test/intl402/NumberFormat/constructor-unit.js",
            "test262/test/intl402/NumberFormat/constructor-options-roundingMode-invalid.js",
            "test262/test/intl402/NumberFormat/constructor-options-throwing-getters-rounding-mode.js",
            "test262/test/intl402/NumberFormat/proto-from-ctor-realm.js",
            "test262/test/intl402/NumberFormat/subclassing.js",
            "test262/test/intl402/NumberFormat/prototype/resolvedOptions/roundingMode.js",
            "test262/test/intl402/NumberFormat/prototype/resolvedOptions/order.js",
            // Expected SameValue(«US$0.00», «+US$0.00») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-zh-TW.js",
            // Expected SameValue(«-0», «0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-rounding.js",
            // Expected SameValue(«0,00 $», «+0,00 $») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-de-DE.js",
            // 0 (always) Expected SameValue(«0», «+0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-de-DE.js",
            // 0 (always) Expected SameValue(«0», «+0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-ko-KR.js",
            // Expected SameValue(«$0.00», «+$0.00») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-en-US.js",
            // Expected SameValue(«$0.00», «+$0.00») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-ja-JP.js",
            //  -0.0001 (exceptZero) Expected SameValue(«-0», «0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-zh-TW.js",
            // 0 (always) Expected SameValue(«0», «+0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-en-US.js",
            // Expected SameValue(«US$0.00», «+US$0.00») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-ko-KR.js",
            // 0 (always) Expected SameValue(«0», «+0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-ja-JP.js",
            // Expected SameValue(«US$0.00», «+US$0.00») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-zh-TW.js",
            // Expected SameValue(«-0», «0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-rounding.js",
            // Expected SameValue(«0,00 $», «+0,00 $») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-de-DE.js",
            // 0 (always) Expected SameValue(«0», «+0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-de-DE.js",
            // 0 (always) Expected SameValue(«0», «+0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-ko-KR.js",
            // Expected SameValue(«$0.00», «+$0.00») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-en-US.js",
            // Expected SameValue(«$0.00», «+$0.00») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-ja-JP.js",
            // Expected SameValue(«-0», «0») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-en-US.js",
            // Expected SameValue(«US$0.00», «+US$0.00») to be true
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-currency-ko-KR.js",
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-ja-JP.js",
            "test262/test/intl402/NumberFormat/prototype/format/units.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-mode-trunc.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-mode-ceil.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-mode-floor.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-mode-half-floor.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-mode-half-even.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-mode-half-trunc.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-mode-half-ceil.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-mode-expand.js",
            "test262/test/intl402/NumberFormat/prototype/format/engineering-scientific-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/format/unit-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/format/notation-compact-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-currency-de-DE.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-de-DE.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-currency-ko-KR.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-ko-KR.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-currency-en-US.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-currency-ja-JP.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-ja-JP.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-currency-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-en-US.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/unit.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/unit-ja-JP.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/unit-ko-KR.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/unit-de-DE.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/unit-en-US.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/unit-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/percent-en-US.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/engineering-scientific-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/notation-compact-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-negative-ja-JP.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-50.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-25.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-negative-currency-en-US.js",
            "test262/test/intl402/NumberFormat/test-option-roundingPriority-mixed-options.js",
            "test262/test/intl402/NumberFormat/prototype/resolvedOptions/return-keys-order-default.js",
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-negative-de-DE.js",
            "test262/test/intl402/NumberFormat/prototype/formatRangeToParts/name.js",
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-negative-en-US.js",
            "test262/test/intl402/NumberFormat/test-option-roundingPriority.js",
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-negative-currency-de-DE.js",
            "test262/test/intl402/NumberFormat/prototype/formatRangeToParts/invoked-as-func.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-negative-currency-ko-KR.js",
            "test262/test/intl402/NumberFormat/prototype/formatRange/invoked-as-func.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-2000.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-5.js",
            "test262/test/intl402/NumberFormat/constructor-options-throwing-getters-trailing-zero-display.js",
            "test262/test/intl402/NumberFormat/prototype/formatRange/builtin.js",
            "test262/test/intl402/NumberFormat/prototype/formatRangeToParts/prop-desc.js",
            "test262/test/intl402/NumberFormat/prototype/formatRange/pt-PT.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-2.js",
            "test262/test/intl402/NumberFormat/prototype/formatRange/nan-arguments-throws.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-negative-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-250.js",
            "test262/test/intl402/NumberFormat/prototype/formatRangeToParts/length.js",
            "test262/test/intl402/NumberFormat/prototype/format/useGrouping-extended-de-DE.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-5000.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-negative-currency-de-DE.js",
            "test262/test/intl402/NumberFormat/throws-for-maximumFractionDigits-over-limit.js",
            "test262/test/intl402/NumberFormat/prototype/formatRange/length.js",
            "test262/test/intl402/NumberFormat/prototype/format/numbering-systems.js",
            "test262/test/intl402/NumberFormat/constructor-roundingIncrement.js",
            "test262/test/intl402/NumberFormat/constructor-options-throwing-getters-rounding-increment.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-negative-ko-KR.js",
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-negative-currency-ko-KR.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-2500.js",
            "test262/test/intl402/NumberFormat/constructor-signDisplay-negative.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-negative-currency-ja-JP.js",
            "test262/test/intl402/NumberFormat/constructor-options-throwing-getters-rounding-priority.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-500.js",
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-negative-currency-zh-TW.js",
            "test262/test/intl402/NumberFormat/prototype/formatToParts/signDisplay-negative-de-DE.js",
            "test262/test/intl402/NumberFormat/prototype/format/value-decimal-string.js",
            "test262/test/intl402/NumberFormat/prototype/formatRangeToParts/nan-arguments-throws.js",
            "test262/test/intl402/NumberFormat/throws-for-minimumFractionDigits-over-limit.js",
            "test262/test/intl402/NumberFormat/prototype/formatRangeToParts/en-US.js",
            "test262/test/intl402/NumberFormat/prototype/formatRange/x-greater-than-y-not-throws.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-increment-200.js",
            "test262/test/intl402/NumberFormat/prototype/format/signDisplay-negative-ja-JP.js",
            "test262/test/intl402/NumberFormat/prototype/formatRangeToParts/x-greater-than-y-not-throws.js",
            "test262/test/intl402/NumberFormat/prototype/formatRange/name.js",
            "test262/test/intl402/NumberFormat/prototype/formatRange/en-US.js",
            "test262/test/intl402/NumberFormat/prototype/format/format-rounding-priority-more-precision.js",
            "test262/test/intl402/NumberFormat/prototype/formatRangeToParts/builtin.js"
        ));

    // Misc
    skipList.addAll(
        Arrays.asList( // We don't throw TypeError from java code yet.
            "test262/test/intl402/Array/prototype/toLocaleString/throws-same-exceptions-as-NumberFormat.js",
            "test262/test/intl402/Number/prototype/toLocaleString/throws-same-exceptions-as-NumberFormat.js",
            "test262/test/built-ins/Array/prototype/toLocaleString/primitive_this_value_getter.js",
            "test262/test/built-ins/Array/prototype/toLocaleString/primitive_this_value.js",
            "test262/test/intl402/Intl/supportedValuesOf"));

    if (android.os.Build.VERSION.SDK_INT < 24) {
      skipList.addAll(
          Arrays.asList(
              "test262/test/intl402/Intl/getCanonicalLocales/unicode-ext-canonicalize-calendar.js",
              "test262/test/intl402/Intl/getCanonicalLocales/unicode-ext-canonicalize-col-strength.js",
              "test262/test/intl402/NumberFormat/prototype/format/unit-ko-KR.js",
              "test262/test/intl402/NumberFormat/prototype/format/unit-de-DE.js",
              "test262/test/intl402/NumberFormat/prototype/format/format-fraction-digits-precision.js",
              "test262/test/intl402/NumberFormat/prototype/format/percent-formatter.js",
              "test262/test/intl402/NumberFormat/prototype/format/format-fraction-digits.js",
              "test262/test/intl402/NumberFormat/prototype/format/unit-ja-JP.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/default-parameter.js",
              "test262/test/intl402/Intl/getCanonicalLocales/unicode-ext-canonicalize-calendar.js",
              "test262/test/intl402/Intl/getCanonicalLocales/unicode-ext-canonicalize-col-strength.js",
              "test262/test/intl402/DateTimeFormat/constructor-options-toobject.js",
              "test262/test/intl402/DateTimeFormat/ignore-invalid-unicode-ext-values.js",
              "test262/test/intl402/DateTimeFormat/prototype/format/related-year-zh.js",
              "test262/test/intl402/DateTimeFormat/prototype/formatToParts/pattern-on-calendar.js",
              "test262/test/intl402/DateTimeFormat/prototype/formatToParts/related-year.js",
              "test262/test/intl402/DateTimeFormat/prototype/formatToParts/related-year-zh.js",
              "test262/test/intl402/DateTimeFormat/prototype/resolvedOptions/basic.js"));
    }

    // ICU:56 	CLDR:28 	UniCode: 8.0
    if (android.os.Build.VERSION.SDK_INT < 26) {
      skipList.addAll(Arrays.asList("unicode-ext-canonicalize-measurement-system.js"));
    }

    // Requires Android 9 (API level 28) ICU: 60.2 	CLDR: 32.0.1 	UniCode: 10.0
    // (https://developer.android.com/guide/topics/resources/internationalization)
    if (android.os.Build.VERSION.SDK_INT < 28) {
      skipList.addAll(
          Arrays.asList(
              "test262/test/intl402/Intl/getCanonicalLocales/unicode-ext-canonicalize-timezone.js",
              "test262/test/intl402/NumberFormat/prototype/format/notation-compact-ko-KR.js",
              "test262/test/intl402/NumberFormat/prototype/format/notation-compact-en-US.js",
              "test262/test/intl402/NumberFormat/prototype/format/notation-compact-ja-JP.js"));
    }

    // Requires Android 10 (API level 29) ICU: 63.2 	CLDR: 34 	UniCode: 11.0
    // (https://developer.android.com/guide/topics/resources/internationalization)
    if (android.os.Build.VERSION.SDK_INT < 29) {
      skipList.addAll(
          Arrays.asList(
              "test262/test/intl402/NumberFormat/constructor-unitDisplay.js",
              "test262/test/intl402/NumberFormat/prototype/format/format-significant-digits.js",
              "test262/test/intl402/NumberFormat/prototype/format/unit-en-US.js",
              "test262/test/intl402/NumberFormat/prototype/format/notation-compact-de-DE.js",
              "test262/test/intl402/NumberFormat/prototype/format/format-significant-digits-precision.js"));
    }
    if (android.os.Build.VERSION.SDK_INT < 30) {
      skipList.addAll(
          Arrays.asList(
              "test262/test/intl402/NumberFormat/prototype/format/engineering-scientific-de-DE.js",
              "test262/test/intl402/NumberFormat/prototype/format/engineering-scientific-ja-JP.js",
              "test262/test/intl402/NumberFormat/prototype/format/numbering-systems.js",
              "test262/test/intl402/NumberFormat/prototype/format/engineering-scientific-ko-KR.js",
              "test262/test/intl402/NumberFormat/prototype/format/engineering-scientific-en-US.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/engineering-scientific-ko-KR.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/notation-compact-zh-TW.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/engineering-scientific-en-US.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/notation-compact-de-DE.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/engineering-scientific-de-DE.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/notation-compact-en-US.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/engineering-scientific-ja-JP.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/notation-compact-ja-JP.js",
              "test262/test/intl402/NumberFormat/prototype/formatToParts/notation-compact-ko-KR.js"));
    }
    return skipList;
  }
}
