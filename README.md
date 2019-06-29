# FITS-Utils
A lightweight pure-java library for performing common image processing operations on images in the Flexible Image Transport System (FITS)
The scope of this project will likely evolve with continued development. The original goal was to store pixels from a digital camera image in a way that involves the least amount of processing and retains maximum level of detail during subsequent processing. Thus, the primary goal for now is to be able to batch convert 16-bit Tiff images to 32-bit float FITS images. The workflow motivating this project is:

1. Take photos with a Nikon digital camera
2. RAW format (Nikon's .nef)
3. Use Nikon's ViewNX-2 to batch convert 
4. 16-bit integer TIFF format
5. Use this library to batch convert
6. 32-bit float FITS format

Dependencies
Still being evaluated. FITS input/output will likely use nom-tam-fits. Reading 16-bit Tiff images might use ImageJ api.
