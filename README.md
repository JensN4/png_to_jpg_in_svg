# Convert SVG embedded PNG raster images to JPG

This small and quick and dirty tool consists of a single Java file. It can be called via

	java -cp "«pathto»/png_to_jpg_in_svg/bin" PNG_to_JPG_in_SVG «file|folder»

It can either process a single SVG «file» or all SVGs transitively contained in «folder».

If an embedded PNG is found, it is replaced with an JPG version. Since JPG does not support transparency, the background is set to white. The compression quality is set to 50%.

# System Requirement

Java 8

# Related Work

Florian Eßer has published a python script doing the same trick at <https://gist.github.com/flesser/3f9c80ff42879cad41bf>.
His tool requires Python and uses the [Python Imaging Library (PIL)](http://www.pythonware.com/products/pil/).

Maybe there is a way of using command line tools such as sed and imagemagick to do the trick, but I haven't found a working script which extracts/replaces the embedded files so that imagemagick can work with that. Alas I didn't find a suitable command in Inkscape.

For the png to jpg conversion, I peeked at <
	https://stackoverflow.com/questions/464825/converting-transparent-gif-png-to-jpeg-using-java>

# License

 Copyright (c) 2017 NumberFour AG.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 	<http://www.eclipse.org/legal/epl-v10.html>