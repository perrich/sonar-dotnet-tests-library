/*
 * SonarQube .NET Tests Library
 * Copyright (C) 2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.dotnet.tests;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import org.sonar.api.BatchExtension;
import org.sonar.api.config.Settings;

import java.io.File;

public class UnitTestResultsAggregator implements BatchExtension {

  private final UnitTestConfiguration unitTestConf;
  private final Settings settings;
  private final VisualStudioTestResultsFileParser visualStudioTestResultsFileParser;
  private final NUnitTestResultsFileParser nunitTestResultsFileParser;
  private final XUnitTestResultsFileParser xunitTestResultsFileParser;

  public UnitTestResultsAggregator(UnitTestConfiguration unitTestConf, Settings settings) {
    this(unitTestConf, settings, new VisualStudioTestResultsFileParser(), new NUnitTestResultsFileParser(), new XUnitTestResultsFileParser());
  }

  @VisibleForTesting
  public UnitTestResultsAggregator(UnitTestConfiguration unitTestConf, Settings settings,
    VisualStudioTestResultsFileParser visualStudioTestResultsFileParser,
    NUnitTestResultsFileParser nunitTestResultsFileParser,
    XUnitTestResultsFileParser xunitTestResultsFileParser) {
    this.unitTestConf = unitTestConf;
    this.settings = settings;
    this.visualStudioTestResultsFileParser = visualStudioTestResultsFileParser;
    this.nunitTestResultsFileParser = nunitTestResultsFileParser;
    this.xunitTestResultsFileParser = xunitTestResultsFileParser;
  }

  public boolean hasUnitTestResultsProperty() {
    return hasVisualStudioTestResultsFile() || hasNUnitTestResultsFile() || hasXUnitTestResultsFile();
  }

  private boolean hasVisualStudioTestResultsFile() {
    return settings.hasKey(unitTestConf.visualStudioTestResultsFilePropertyKey());
  }

  private boolean hasNUnitTestResultsFile() {
    return settings.hasKey(unitTestConf.nunitTestResultsFilePropertyKey());
  }
  
  private boolean hasXUnitTestResultsFile() {
    return settings.hasKey(unitTestConf.xunitTestResultsFilePropertyKey());
  }

  public UnitTestResults aggregate(WildcardPatternFileProvider wildcardPatternFileProvider, UnitTestResults unitTestResults) {
    if (hasVisualStudioTestResultsFile()) {
      aggregate(wildcardPatternFileProvider, settings.getString(unitTestConf.visualStudioTestResultsFilePropertyKey()), visualStudioTestResultsFileParser, unitTestResults);
    }

    if (hasNUnitTestResultsFile()) {
      aggregate(wildcardPatternFileProvider, settings.getString(unitTestConf.nunitTestResultsFilePropertyKey()), nunitTestResultsFileParser, unitTestResults);
    }

    if (hasXUnitTestResultsFile()) {
      aggregate(wildcardPatternFileProvider, settings.getString(unitTestConf.xunitTestResultsFilePropertyKey()), xunitTestResultsFileParser, unitTestResults);
    }
    return unitTestResults;
  }

  private void aggregate(WildcardPatternFileProvider wildcardPatternFileProvider, String reportPaths, UnitTestResultsParser parser, UnitTestResults unitTestResults) {
    for (String reportPathPattern : Splitter.on(',').trimResults().omitEmptyStrings().split(reportPaths)) {
      for (File reportFile : wildcardPatternFileProvider.listFiles(reportPathPattern)) {
        parser.parse(reportFile, unitTestResults);
      }
    }
  }

}
