
This configure.xml was from:

PATTERN FACTORY 2.0.0rc24

core/pflib/components/be/2.0.0/configure/configure.xml




A search of PF200rc24 found these BW related configure.xml files.  The components/bw/2.0.0 matches what PF 2.0.0rc24 setup for Bw components so that's the one I used



336731@COS-336731-L1 /cygdrive/c/tibco.svn/PatternFactory-v2.0.0rc24
$ find . -type f -print0 | xargs -0 grep -l '\-\- BW Config \-\-'
./user_factories/L1-pfhome_2.0.0rc24/core/pflib/components/bw/2.0.0/configure/configure.xml
./user_factories/L1-pfhome_2.0.0rc24/core/pflib/patterns/DEV TRA PATTERN/2.0.0/BC/configure/configure.xml
./user_factories/L1-pfhome_2.0.0rc24/core/pflib/patterns/DEV TRA PATTERN/2.0.0/BW/configure/configure.xml



336731@COS-336731-L1 /cygdrive/c/tibco.svn/PatternFactory-v2.0.0rc24
$ ls -alF user_factories/L1-pfhome_2.0.0rc24/core/pflib/components/bw/2.0.0/configure/configure.xml
-rwx------+ 1 Administrators mkgroup 8010 Jan 25 11:20 user_factories/L1-pfhome_2.0.0rc24/core/pflib/components/bw/2.0.0/configure/configure.xml*

336731@COS-336731-L1 /cygdrive/c/tibco.svn/PatternFactory-v2.0.0rc24
$ ls -alF "user_factories/L1-pfhome_2.0.0rc24/core/pflib/patterns/DEV TRA PATTERN/2.0.0/BC/configure/configure.xml"
-rwx------+ 1 Administrators mkgroup 7848 Jan 25 11:20 user_factories/L1-pfhome_2.0.0rc24/core/pflib/patterns/DEV TRA PATTERN/2.0.0/BC/configure/configure.xml*

336731@COS-336731-L1 /cygdrive/c/tibco.svn/PatternFactory-v2.0.0rc24
$ ls -alF "user_factories/L1-pfhome_2.0.0rc24/core/pflib/patterns/DEV TRA PATTERN/2.0.0/BW/configure/configure.xml"
-rwx------+ 1 Administrators mkgroup 7854 Jan 25 11:20 user_factories/L1-pfhome_2.0.0rc24/core/pflib/patterns/DEV TRA PATTERN/2.0.0/BW/configure/configure.xml*


