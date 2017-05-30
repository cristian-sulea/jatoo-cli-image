/*
 * Copyright (C) Cristian Sulea ( http://cristian.sulea.net )
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jatoo.cli.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import jatoo.cli.AbstractCommand;
import jatoo.image.ImageFileFilter;
import jatoo.image.ImageUtils;

public class JatooCLICommand extends AbstractCommand {

  @Override
  public void execute(final String[] args) {

    //
    // options

    OptionGroup optionGroup = new OptionGroup();
    optionGroup.setRequired(true);
    optionGroup.addOption(Option.builder("resize").desc(getText("desc.option.resize")).build());
    optionGroup.addOption(Option.builder("crop").desc(getText("desc.option.crop")).build());
    optionGroup.addOption(Option.builder("rotate").desc(getText("desc.option.rotate")).build());

    Options options = new Options();
    options.addOptionGroup(optionGroup);

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      if (line.hasOption("resize")) {
        resize(line.getArgs());
      }

      else if (line.hasOption("crop")) {
        // resize(line.getArgs());
      }

      else if (line.hasOption("rotate")) {
        // resize(line.getArgs());
      }

      else {
        throwUnknownOption();
      }
    }

    catch (Throwable t) {
      printHelp("-image", options, t);
    }
  }

  private void resize(final String[] args) {

    //
    // options

    OptionGroup optionGroup = new OptionGroup();
    optionGroup.setRequired(true);
    optionGroup.addOption(Option.builder("fit").desc(getText("desc.option.resize.fit")).build());
    optionGroup.addOption(Option.builder("fill").desc(getText("desc.option.resize.fill")).build());

    Options options = new Options();
    options.addOptionGroup(optionGroup);

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      if (line.hasOption("fit")) {
        resize(line.getArgs(), true);
      }

      else if (line.hasOption("fill")) {
        resize(line.getArgs(), false);
      }

      else {
        throwUnknownOption();
      }
    }

    catch (Throwable e) {
      printHelp("-image -resize", options, e);
    }
  }

  private void resize(final String[] args, boolean fit) {

    //
    // options

    Options options = new Options();
    options.addOption(Option.builder("width").hasArg().required(true).desc(getText("desc.option.resize." + (fit ? "fit" : "fill") + ".width")).build());
    options.addOption(Option.builder("height").hasArg().required(true).desc(getText("desc.option.resize." + (fit ? "fit" : "fill") + ".height")).build());
    options.addOption(Option.builder("in").hasArg().required(true).desc(getText("desc.option.resize.in")).build());
    options.addOption(Option.builder("out").hasArg().required(true).desc(getText("desc.option.resize.out")).build());
    options.addOption(Option.builder("removeMetadata").required(false).desc(getText("desc.option.resize.removeMetadata")).build());
    options.addOption(Option.builder("overwrite").required(false).desc(getText("desc.option.resize.overwrite")).build());

    //
    // parse

    try {

      CommandLine line = parse(options, args, true);

      //
      // and work

      int width = Integer.parseInt(line.getOptionValue("width"));
      int height = Integer.parseInt(line.getOptionValue("height"));
      File in = new File(line.getOptionValue("in"));
      File out = new File(line.getOptionValue("out"));
      boolean removeMetadata = line.hasOption("removeMetadata");
      boolean overwrite = line.hasOption("overwrite");

      if (!in.exists()) {
        throw new FileNotFoundException("input does not exists: " + in.getAbsolutePath());
      }

      if (!out.exists()) {
        out.mkdirs();
      }
      if (!out.isDirectory()) {
        throw new NotDirectoryException(out.getAbsolutePath());
      }

      if (in.isFile()) {

        File inImageFile = in;
        File outImageFile = new File(out, inImageFile.getName());

        if (!overwrite) {
          if (outImageFile.exists()) {
            throw new FileAlreadyExistsException(outImageFile.getPath(), null, "file already exists" + System.getProperty("line.separator") + "use \"-overwrite\" option to overwrite existing file)");
          }
        }

        System.out.println(getText("text.resizing.1.image", inImageFile.getName()));

        ImageUtils.resizeTo(fit, inImageFile, outImageFile, width, height, !removeMetadata);

        System.out.println(getText("text.done"));
      }

      else if (in.isDirectory()) {

        File[] inImageFiles = in.listFiles(ImageFileFilter.getInstance());

        if (!overwrite) {
          for (File inImageFile : inImageFiles) {
            File outImageFile = new File(out, inImageFile.getName());
            if (outImageFile.exists()) {
              throw new FileAlreadyExistsException(outImageFile.getPath(), null, "file already exists" + System.getProperty("line.separator") + "use \"-overwrite\" option to overwrite existing files");
            }
          }
        }

        System.out.println(getText("text.resizing.n.images.1", inImageFiles.length, in.getPath()));

        for (File inImageFile : inImageFiles) {
          File outImageFile = new File(out, inImageFile.getName());

          ImageUtils.resizeTo(fit, inImageFile, outImageFile, width, height, !removeMetadata);

          System.out.println(getText("text.resizing.n.images.2", outImageFile.getName()));
        }

        System.out.println(getText("text.done"));
      }

      else {
        throw new IllegalArgumentException("illegal input");
      }
    }

    catch (Throwable e) {
      printHelp("-image -resize -" + (fit ? "fit" : "fill"), options, e);
    }
  }

}
