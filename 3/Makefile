ARCHIVE=ds3.zip

package:
	find \( ! -regex '^./build/.*' -a ! -regex '^./lib/.*' \) | zip $(ARCHIVE) -@

clean:
	rm -f $(ARCHIVE)
