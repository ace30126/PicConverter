# ImageConverter

A simple image format conversion tool.

## Overview

ImageConverter is a lightweight desktop application that allows you to easily convert images between different formats. Built with Java and Swing, it provides a user-friendly graphical interface for quick and efficient image conversion.

**Key Features:**

* Simple and intuitive GUI.
* Select the desired output format from the top button group.
* Convert images by either selecting files or using drag and drop in the bottom area.
* Supports various image formats (determined by the `image4j` library).

## Installation

As this is a standalone executable (`ImageConverter.exe`), no formal installation is required. Simply download the file and run it.

## Usage

Double-click `ImageConverter.exe` to launch the application.

1.  **Select Output Format:** Choose the desired output image format by clicking one of the buttons in the top group.
2.  **Select Images:** You can add images for conversion in two ways:
    * Click the "Select Images" button (label may vary slightly) and choose the image files from your computer.
    * Drag and drop image files directly into the designated area at the bottom of the window.
3.  The conversion process will likely start automatically or with a confirmation button. The converted images will be saved in a default output location (which may be in the same directory as the original files or a designated output folder).

## Built With

* [JAVA](https://www.java.com/) - The programming language used.
* [Swing](https://docs.oracle.com/javase/8/docs/api/javax/swing/package-summary.html) - The GUI toolkit for Java.
* [image4j](https://github.com/luciad/image4j) [LGPL] - A Java library for reading and writing image files. **Note the LGPL license of this library.**

## Author

* REDUCTO (https://tutoreducto.tistory.com/640)

## Creation Date

* April 21, 2025 (1 day of development)

## Version

* v1.0

## Acknowledgements

* This program was created with the assistance of Gemini.

---

Remember to replace placeholders like "[Select Images]" with the actual button label in your application. You might also want to specify the supported input and output formats based on the capabilities of the `image4j` library.
