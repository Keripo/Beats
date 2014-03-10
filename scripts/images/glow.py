#from PIL import Image; orig = Image.open("arrow_left_24.png"); orig.show()



from PIL import Image
import numpy as np
import scipy as sp
from scipy import signal as sig
from pystart import imshow
from glob import glob

def sum_1(x): return x/sum(x)

def gaus_ker(std): return sum_1(sig.gaussian(std*6, std))*sig.ones((1,1))

def add_dim(one_channel): return one_channel.reshape(one_channel.shape + (1,))



def glow(orig, vertical):
	horiz_blur = .5 if vertical else 1.5
	vert_blur = 1.5 if vertical else .5
	orig = np.asarray(orig)
	orig_img = orig[:,:,0:3]
	glowcolor = np.array([255, 255, 255]) # white
	# glowcolor = orig_img.mean(0).mean(0) # same
	# glowcolor = 255 - orig_img.mean(0).mean(0) # inverse
	orig_mask = orig[:,:,3] / 255.0
	blur_mask = sig.convolve(orig_mask, gaus_ker(horiz_blur), 'same')
	blur_mask = sig.convolve(blur_mask, gaus_ker(vert_blur).T, 'same')
	orig_mask = add_dim(orig_mask)
	composite_img = orig_img * orig_mask + np.array(255*glowcolor/max(glowcolor)).reshape((1,1,3)) * (1-orig_mask)
	composite_mask = 1-(1-blur_mask)**3
	out = Image.fromarray(composite_img.clip(0,255).astype(np.uint8))
	out.putalpha(Image.fromarray((composite_mask*255).clip(0,255).astype(np.uint8)))
	return out

for (direction, vertical) in (("right", True), ("left", True), ("up", False), ("down", False)):
	for filename in glob("arrow_%s_*.png" % direction):
		if any(x in filename for x in ["hit", "control", "192"]): continue
		orig = Image.open(filename)
		glow(orig, vertical).save("out/" + filename, "PNG")