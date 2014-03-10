import Image, glob, ImageChops, ImageMath, ImageEnhance
files = glob.glob("arrow_*_4.png")

def incrange(start, stop, step):
	if step > 0:
		return range(start, stop+1, step)
	else:
		return range(start, stop-1, step)

def channelMap(f, image):
	return Image.merge(image.mode, [f(chan) for chan in image.split()])
	#pun not intended

class NotBrokenRGBA:
	""" grrr PIL. """
	def __init__(self, size):
		self.white = Image.new("RGB", size, (255,255,255))
		self.black = Image.new("RGB", size, (0,0,0))

	def paste(self, *args):
		self.white.paste(*args)
		self.black.paste(*args)

	def pasteRGBA(self, pasta, location):
		self.paste(pasta, location, pasta)

	def create(self):
		mask = ImageChops.subtract(self.white, self.black, -1, 255)
		maskgray = mask.convert("L")

		#out = self.black.convert("RGBA") # this doesn't premultiply alpha correctly?
		#this better?
		def divide(ch):
			env = {"val": ch, "mask": maskgray}
			return ImageMath.eval("val*255/mask",env).convert("L")
		out = channelMap(divide, self.black).convert("RGBA")

		out.putalpha(maskgray)		
		return out

	def save(self, *args):
		self.create().save(*args)

for filename in files:
	filesuffix = filename.replace("arrow","",1)
	im = Image.open(filename)
	im_alpha = (im.copy().split())[-1]
	im_bright = ImageEnhance.Brightness(im).enhance(2)
	im_dark = ImageEnhance.Brightness(im).enhance(.5)
	im_weak = ImageEnhance.Color(im_dark).enhance(.2)
	im_bright.putalpha(im_alpha)
	im_weak.putalpha(im_alpha)
	im_dark.putalpha(im_alpha)

	im_middle = im_weak # depends on active/inactive?
	
	(imw, imh) = im.size
	skippix = 4 # should be factor of imh (typically 64)

	holdtypes = {"_active": im_bright, "_inactive": im_dark, "_dead": im_weak}
	for im_middle_name, im_middle in holdtypes.iteritems():
		
		# stepmania has bottomcap, but we're scrolling the other direction
		htopcap = NotBrokenRGBA((imw, imh)) #Image.new("RGBA", (imw, imh))
		hbody = NotBrokenRGBA((imw, 2*imh)) # Image.new("RGB", (imw, 2*imh))

		for y in incrange(imh*2, 0, -skippix):
			htopcap.pasteRGBA(im if y==0 else im_middle, (0, y))
		for y in incrange(imh*4, -imh*4, -skippix):
			hbody.pasteRGBA(im_middle, (0, y))
	
		hbody.save("hbody" + im_middle_name + filesuffix, "PNG")
		htopcap.save("htopcap" + im_middle_name + filesuffix, "PNG")

