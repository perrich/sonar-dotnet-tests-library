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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class XUnitTestResultsFileParser implements UnitTestResultsParser {

  private static final Logger LOG = LoggerFactory.getLogger(XUnitTestResultsFileParser.class);

  @Override
  public void parse(File file, UnitTestResults unitTestResults) {
    LOG.info("Parsing the XUnit Test Results file " + file.getAbsolutePath());
    new Parser(file, unitTestResults).parse();
  }

  private static class Parser {

    private final File file;
    private XmlParserHelper xmlParserHelper;
    private final UnitTestResults unitTestResults;
	
	private boolean isSecondVersion = false;

    public Parser(File file, UnitTestResults unitTestResults) {
      this.file = file;
      this.unitTestResults = unitTestResults;
    }

    public void parse() {
      try {
        xmlParserHelper = new XmlParserHelper(file);
        checkRootTag();
        handleTestResultsTag();
      } finally {
        if (xmlParserHelper != null) {
          xmlParserHelper.close();
        }
      }
    }

    private void checkRootTag() {
	  String rootTag = xmlParserHelper.nextTag();
	  
	  if ("assemblies".equals(rootTag)) {
		isSecondVersion = true;
	    rootTag = xmlParserHelper.nextTag();
	  }

      if (!"assembly".equals(rootTag)) {
        throw xmlParserHelper.parseError("Missing needed element <assembly>");
      }
    }

    private void handleTestResultsTag() {
      int tests = xmlParserHelper.getRequiredIntAttribute("total");
      int skipped = xmlParserHelper.getRequiredIntAttribute("skipped");
      int passed = xmlParserHelper.getRequiredIntAttribute("passed");
      int failures = xmlParserHelper.getRequiredIntAttribute("failed");
	  int errors = 0;
	  
	  if (isSecondVersion) {
		errors = xmlParserHelper.getRequiredIntAttribute("errors");
	  }	  

      unitTestResults.add(tests, passed, skipped, failures, errors);
    }
  }

}
