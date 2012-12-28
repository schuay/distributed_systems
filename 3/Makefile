ARCHIVE=ds3.zip

package:
	find \( ! -regex '^./build/.*' -a ! -regex '^./lib/log4j.*' \) | zip $(ARCHIVE) -@

clean:
	rm -f $(ARCHIVE)
