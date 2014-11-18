find . -type f -print0 | xargs -0 -I {} ln -s ../../third_party/ShowcaseView/library/res/{}  ../../../../res/{}
