README

1) How to setup Peerbox with SWT

Download the latest SWT library from http://www.eclipse.org/swt for your operating system. Add the library to your build path.
If you are running OS X, you must add the JVM variable -XstartOnFirstThread else you will get a error stating that SWT has to be started from the main thread. Add this variable to the run configurations. 

By default it downloads the 32bit version, probably you have to change that to the 64bit version of SWT. If you get an exception stating that ST could not load 32bit libraries, you have chosen the wrong version.

