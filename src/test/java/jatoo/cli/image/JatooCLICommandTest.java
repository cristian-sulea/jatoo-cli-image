/*
 * Copyright (C) Cristian Sulea ( http://cristian.sulea.net )
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jatoo.cli.image;

import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import jatoo.image.ImageUtils;

public class JatooCLICommandTest {

  @Test
  public void testResizeFit() throws Exception {

    new JatooCLICommand().execute(
      new String[] {
          "-resize",
          "-fit",
          "-width", "400",
          "-height", "200",
          "-removeMetadata",
          "-overwrite",
          "-src", "src/test/resources/jatoo/cli/image/20141109144518.jpg",
          "-dst", "target/tests/"
      });

    BufferedImage image = ImageUtils.read("target/tests/20141109144518.jpg");

    Assert.assertTrue(image.getWidth() <= 400);
    Assert.assertTrue(image.getHeight() <= 200);
  }

  @Test
  public void testResizeFill() throws Exception {

    new JatooCLICommand().execute(
      new String[] {
          "-resize",
          "-fill",
          "-width", "400",
          "-height", "200",
          "-removeMetadata",
          "-overwrite",
          "-src", "src/test/resources/jatoo/cli/image/20141109144518.jpg",
          "-dst", "target/tests/"
      });

    BufferedImage image = ImageUtils.read("target/tests/20141109144518.jpg");

    Assert.assertTrue(image.getWidth() == 400);
    Assert.assertTrue(image.getHeight() == 200);
  }

  @Test
  public void testResizeFolder() throws Exception {

    new JatooCLICommand().execute(
      new String[] {
          "-resize",
          "-fit",
          "-width", "400",
          "-height", "200",
          "-removeMetadata",
          "-overwrite",
          "-src", "src/test/resources/jatoo/cli/image/",
          "-dst", "target/tests/"
      });

    Assert.assertTrue(new File("src/test/resources/jatoo/cli/image/").list().length <= new File("target/tests/").list().length);
  }

  @Test
  public void testRename1() throws Exception {

    new JatooCLICommand().execute(
      new String[] {
          "-rename",
          "-pattern", "yyMMddHHmmss",
          "-src", "src/test/resources/jatoo/cli/image/20141109144518.jpg",
          "-dst", "target/tests-rename1/"
      });

    Assert.assertTrue(new File("target/tests-rename1/").listFiles()[0].getName().length() == 16);
  }

  @Test
  public void testRename2() throws Exception {

    new JatooCLICommand().execute(
      new String[] {
          "-rename",
          "-pattern", "yyyyMMdd-HHmmss-${counter}",
          "-counterDigits", "5",
          "-src", "src/test/resources/jatoo/cli/image/",
          "-dst", "target/tests-rename2/"
      });

    Assert.assertTrue(new File("src/test/resources/jatoo/cli/image/").list().length <= new File("target/tests-rename2/").list().length);
  }

  @Test
  public void testMetadataGet() throws Exception {

    new JatooCLICommand().execute(
      new String[] {
          "-metadata",
          "-src", "target/test-classes/jatoo/cli/image/20141109144518.jpg",
          "-get", "-DateTimeOriginal"
      });

//    Assert.assertTrue(new File("target/tests-rename1/").listFiles()[0].getName().length() == 16);
  }
  
}
