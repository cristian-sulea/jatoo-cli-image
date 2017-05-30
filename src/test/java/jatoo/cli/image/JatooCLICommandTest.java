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

import jatoo.cli.JatooCLI;

public class JatooCLICommandTest {

  public static void main(String[] args) {
    
    new JatooCLI().execute(
        new String[] {
            "-image",
            "-resize",
            "-fit",
            "-width", "800",
            "-height", "800",
            "-in", "src/test/resources/jatoo/cli/image/20141109144518.jpg",
            "-out", "target/tests",
            "-removeMetadata",
            "-overwrite" });
    
    System.out.println();
    System.out.println("====================================================================================================");
    System.out.println();
    
    new JatooCLI().execute(
        new String[] {
            "-image",
            "-resize",
            "-fit",
            "-width", "800",
            "-height", "800",
            "-in", "src/test/resources/jatoo/cli/image/",
            "-out", "target/tests",
            "-removeMetadata",
            "-overwrite" });
  }

}
