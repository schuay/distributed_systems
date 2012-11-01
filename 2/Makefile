ARCHIVE=ds2.zip

package:
	find \( ! -regex '^./build/.*' \) | zip $(ARCHIVE) -@

clean:
	rm -f $(ARCHIVE)
