import Image, glob
files = glob.glob("*.png")
for filename in files:
	im = Image.open(filename)
	rotations = {"up": 0, "left":90, "down":180, "right":270}
	for name, deg in rotations.iteritems():
		im.rotate(deg).save("out/arrow_%s_%s" % (name,filename), "PNG")

