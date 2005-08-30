#!/bin/bash

cd ..
#ant runtime-mac
cd macosx
#freeze tvbrowser.packproj
./mkdmg -v -format UDZO -s build/.app.pkg build/.core.pkg build/install.mpkg build/readme.rtf -i ../runtime/tvbrowser_mac/tvbrowser.dmg

