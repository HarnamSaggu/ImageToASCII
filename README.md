# ImageToASCII
Converts an image to an ASCII representation<br><br>

By giving the filepath of the image the desired resolution for the ASCII conversion and a char width and map you can convert an image to ASCII art.
<br>
The resolution specifies the dimensions of the subimages which decide the ASCII char, res = 12 would mean 12px x 12px squares are used to determine the corresponding char.
<br>
The char width is how many times the char should be repeated as one character is not a square the char width allows you to conpensate for the extra height.
<br>
The map is a set of chars which are arranged in order of light -> dark, these are the chars used for the image.
<br><br>
Enjoy :)

<br><br><br>

I have a example in src/main/resources, its the s. of liberty in ASCII form woth its orginal image. The ASCII form is quite large so look at its raw view :)
