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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class XUnitTestResultsFileParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void no_counters() {
    thrown.expect(ParseErrorException.class);
    thrown.expectMessage("Missing attribute \"total\" in element <assembly> in ");
    thrown.expectMessage(new File("src/test/resources/xunit/no_counters.xml").getAbsolutePath());
    new XUnitTestResultsFileParser().parse(new File("src/test/resources/xunit/no_counters.xml"), mock(UnitTestResults.class));
  }

  @Test
  public void wrong_passed_number() {
    thrown.expect(ParseErrorException.class);
    thrown.expectMessage("Expected an integer instead of \"invalid\" for the attribute \"total\" in ");
    thrown.expectMessage(new File("src/test/resources/xunit/invalid_total.xml").getAbsolutePath());
    new XUnitTestResultsFileParser().parse(new File("src/test/resources/xunit/invalid_total.xml"), mock(UnitTestResults.class));
  }

  @Test
  public void valid() throws Exception {
    UnitTestResults results = new UnitTestResults();
    new XUnitTestResultsFileParser().parse(new File("src/test/resources/xunit/valid.xml"), results);

    assertThat(results.tests()).isEqualTo(200);
    assertThat(results.passedPercentage()).isEqualTo(160 * 100.0 / 200);
    assertThat(results.skipped()).isEqualTo(10);
    assertThat(results.failures()).isEqualTo(30);
    assertThat(results.errors()).isEqualTo(0);
  }
  
  @Test
  public void validSecondVersionFile() throws Exception {
    UnitTestResults results = new UnitTestResults();
    new XUnitTestResultsFileParser().parse(new File("src/test/resources/xunit/valid2.xml"), results);

    assertThat(results.tests()).isEqualTo(45);
    assertThat(results.passedPercentage()).isEqualTo(28 * 100.0 / 45);
    assertThat(results.skipped()).isEqualTo(10);
    assertThat(results.failures()).isEqualTo(5);
    assertThat(results.errors()).isEqualTo(2);
  }

}
