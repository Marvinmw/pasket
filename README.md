# Pasket

Pasket is a *pa*ttern-based *sket*ching tool that leverages design patterns
so as to synthesize models for event-driven frameworks, such as Swing and Android.


## Publications

* [Synthesizing Framework Models for Symbolic Execution.][icse16]
  Jinseong Jeon, Xiaokang Qiu, Jonathan Fetter-Degges, Jeffrey S. Foster, and Armando Solar-Lezama.
  In _38th International Conference on Software Engineering (ICSE '16)_, May 2016.

[icse16]: http://dx.doi.org/10.1145/2884781.2884856


## Requirements

* Sketch

The main feature of this tool is to translate high-level templates into
low-level ones in [Sketch][sk169].  Thus, you have to install it first.
Download the tar ball and follow the instruction in it.
You may need to set your environment variables as follows:

```sh
    export SKETCH_HOME=/path/to/sketch/runtime
    export PATH=$PATH:$SKETCH_HOME/..
```

A harder way is to install it from source code:
[front-end][sk-front]/[back-end][sk-back].
In that case, build architecture-independent version of [front-end][sk-front]
via:

```sh
    <sketch-frontend> $ make assemble-noarch
```

and then set your environment variables as follows:

```sh
    export SKETCH_HOME=/path/to/sketch-frontend
    export PATH=$PATH:$SKETCH_HOME/target/sketch-1.7.0-noarch-launchers
```

* Apache Ant

There are several Java applications in this project.  To build them
efficiently, your system needs to have [Apache Ant][ant] installed.

* Python

This tool is tested under [Python 2.7.1][py271].

[sk169]: http://people.csail.mit.edu/asolar/sketch-1.6.9.tar.gz
[sk-front]: https://bitbucket.org/gatoatigrado/sketch-frontend
[sk-back]: https://bitbucket.org/gatoatigrado/sketch-backend
[ant]: http://ant.apache.org
[py271]: http://www.python.org/download/releases/2.7/


## Usage (Tool)

### Parser Generation

Auto-generated files are not maintained under the version control.
So, generate the lexer and parser first:
```sh
$ python -m grammar.gen [-g Java.g]
```
or
```sh
$ ./grammar/gen.py [-g Java.g]
```

The default ANTLR grammar file is `Java.g`, which is modified to allow
annotations in an expression level.  The command above will generate
the lexer and parser in `grammar/` folder.


### Custom Codegen

Next, compile the custom code generator for sketch:
```sh
    $ ./run.py -c codegen
```

Note that your env should have variable `$SKETCH_HOME` as mentioned above
so that the build process can refer to sketch jar file.
You can also use the following commands, if preferred:
```sh
    $ cd codegen; ant; cd ..
```

### Model Synthesis

Then, synthesize a framework model:
```sh
    $ ./run.py -c (android|gui) [-s sample] [-t template] [-p pattern] [-o result]
```

Inputs for synthesis are samples and templates.  The default paths for
samples and templates depend on the command.  For example, if the command
is "android", the default path for sample is `sample/android/` folder.
You can pass a single file, e.g.,
```sh
    $ ./run.py -s sample/android/remotedroid.txt
```

Otherwise, the tool will read all the samples in the given path,
e.g., `sample/android/*/*.txt`.  (`sample/android/README.md` explains
how to use `sample/android/trim.py` in order to obtain such samples
from apps instrumented by [redexer][redexer].)

Similarly, the default paths for template are `template/android/` and
`template/app/android/` folders.  The former includes framework modelings,
while the latter has client code, i.e., tutorials.
You can pass multiple templates, e.g.,
```sh
    $ ./run.py -t template/android -t template/app/android
```

By default, the tool will try all possible design patterns.  If performance
does matter, you can hint which design patterns are used in the template:
```sh
    $ ./run.py -p observer -p state
```

Using both samples and templates, the tool will encode the problem into
sketches, and those intermediate files will be left at `result/(java_)sk*/` folder.
The final synthesized code will be placed at `result/java/` folder.

To synthesize Java GUI model, run the following command:
```sh
    $ ./run.py -c gui -p button_demo -p checkbox_demo ... [opts]
```

For Android model, run the following command:
```sh
    $ ./run.py -c android -p button -p checkbox ... [opts]
```

Notice that you need `-p` option for every demo you pass.

There are more options for debugging purpose:

    --no-encoding: proceed without the encoding phase,
                   to test manipulated sketch files
    --no-sketch: examine the process without running sketch,
                 rather it will use previous output)

You can simulate a certain demo using the synthesized model:
```sh
    $ ./run.py -c gui -p button_demo -p colorchooser_demo --simulate colorchooser_demo2
```

where the first two demos are used for synthesizing a model,
and then the third one is simulated on top of that synthesized model.
If you pass the same demo as synthesis input and simulation target,
the tool will perform so-called _sanity checking_:
```sh
    $ ./run.py -c gui -p button_demo --simulate button_demo
```
which is just same as this:
```sh
    $ ./run.py -c gui -p button_demo --sanity
```

To run (deprecated) design pattern examples, use the following commands:
```sh
    $ ./run.py -c pattern -p observer
    $ ./run.py -c pattern -p state
    $ ./run.py -c pattern -p singleton
```

The existing examples can be tested as follows:
```sh
    $ ./test/test.py pattern observer
    $ ./test/test.py gui button_demo [checkbox_demo ...]
```

[redexer]: http://www.cs.umd.edu/projects/PL/redexer/


## Usage (Model)

### Swing

To run Java PathFinder (JPF) together with the synthesized model,
install `jpf-core`, `jpf-symbc`, and `jpf-awt` first.
In what follows, we assume those projects are installed
under `user.home/Downloads/jpf` directory.
Then, copy `jpf-awt` into `jpf-awt-synth`,
where the synthesized model will be placed.
To copy such synthesized model into that folder, run as follows:
```sh
  ...result $ ant gui
```

Next, go to `jpf-awt-synth` and build it same as other jpf-* projects.

JPF has its own event generating mechanism;
refer to `example/gui/src/oreilly/ch*/*Test.java`
Classpath to compile JPF test classes is set up in `example/gui/build.xml`,
so place your own target in that build.xml when you add new applications.
Also, generate app-specific test class accordingly.
To build examples and applications, refer to `example/gui/README.md`.

To run `jpf-symbc` both with `jpf-awt` and `jpf-awt-synth`,
you need to design configurations like:
`example/gui/src/oreilly/ch*/*.awt(-synth).jpf`
Then, run `jpf-symbc/bin/jpf`, passing paths to those configurations.


### Android

To be added...


## Structure

- Java.g -- an ANTLR grammar file for Java
- README.md -- the file you're currently reading
- antlr3/ -- Python wrapper for ANTLR parser generator
- codegen/ -- custom code generator for sketch (use '-c codegen' command)
    + build.xml -- for ant builder
    + lib/ -- where codegen.jar will be created
    + src/ -- source of custom code generator
        * CSV.java -- printing expressions in a csv format
- example/ -- example code
    + android -- Android examples/apps
        * README.md -- explaining how to use sample/android/trim.py
        * trim.py -- a script to capture adb logcats
    + gui -- Swing examples/apps
        * apps.json -- application descriptions
        * build.xml -- for ant builder
        * log\_method.d -- capturing call sequences in Java via dtrace
        * README.md -- explaining how to build examples and use example/run.py
        * run.py -- a script to run and log examples
        * src/ -- source of Swing examples
- grammar/ -- Java parser generated by ANTLR (use '-c grammar' command)
    + \_\_init\_\_.py -- to make this folder a library
    + gen.py -- a script to generate lexer and parser from the grammar
    + Java.tokens -- (a variety of terminals, generated by ANTLR)
    + JavaLexer.py -- (lexer, generated by ANTLR)
    + JavaParser.py -- (parser, generated by ANTLR)
- lib/ -- external libraries
    + \_\_init\_\_.py -- to make this folder a library
    + antlr-3.1.3.jar -- ANTLR parser generator
    + const.py -- a module to make Java-like const
    + glob2/ -- a library that supports a recursive '\*\*' globbing syntax
    + hamcrest-core-1.3.jar -- a framework for writing matcher objects
    + junit-4.11.jar -- JUnit testing framework
    + typecheck.py -- a set of decorators to check types of functions
    + visit.py -- decorators for visitor pattern
- logger/ -- Java logger based on javassit instrument
    + build.xml -- for ant builder
    + Manifest.mf -- manifest file to make a jar file for the call logger
    + lib/ -- where loggeragent.jar will be created
        * javassist.jar -- Java instrument tool
    + src/ -- source of Java logger
- pyclean -- a script to delete all .pyc files recursively
- result/ -- result folder
    + build.xml -- for ant builder
    + clean.sh -- a script to clean result files
    + java/ -- final synthesis results will be placed here
    + java\_sk/ -- intermediate Java sketch files will be placed here
    + output/ -- sketch output files
    + rename.sh -- a script to rename packages in synthesized Java files
    + sk/ -- sketch files will be placed here
        * type.sk -- containing all the class declarations
        * log.sk -- a logging module, along with class and method ids
        * class\_x.sk -- corresponding to a single class and its methods
        * sample.sk -- the main harness, including all other sketch files
        * sample\_x.sk -- corresponding to a single sample (call-return seq.)
- run.py -- the main script to run the tool
- sample/ -- samples
    + android/ -- samples by running some Android apps
        * \*.txt -- a sample representing a single run
    + gui/ -- samples by running Swing apps
    + pattern/ -- samples by running examples in pattern/ folder
- pasket/ -- main source tree
    + \_\_init\_\_.py -- main entry point of this tool
    + analysis/
        * \_\_init\_\_.py
        * api.py -- collecting API usages in the given demo(s)
        * cover.py -- checking if the template covers API usages in demo(s)
        * empty.py -- measuring how many methods have empty body
    + anno.py -- parsing annotations in templates
    + decode/ -- pattern-specific synthesis interpretation
        * \_\_init\_\_.py -- generating a model
        * accessor.py -- accessor pattern
        * collection.py -- replacing interfaces with implementing classes
        * observer.py -- observer pattern
    + encoder.py -- translating high-level templates into low-level sketches
    + harness.py -- generating harness methods from the samples
    + logger.py -- logging pasket behavior
    + logging.conf -- logging configuration
    + meta -- meta-classes, along with utilities
        * \_\_init\_\_.py
        * clazz.py
        * expression.py
        * field.py
        * method.py
        * statement.py
        * template.py
    + psketch.py -- running Sketch in parallel
    + reducer.py -- reducing annotations in templates
    + rewrite/ -- pattern-specific rewriting rules
        * \_\_init\_\_.py -- including a base iterator
        * accessor.py -- basic accessors
        * android/ -- platform-specific reductions
        * builder.py -- builder pattern
        * factory.py -- factory pattern
        * gui/ -- platform-specific reductions
        * observer.py -- observer pattern
        * proxy.py -- proxy pattern
        * singleton.py -- singleton pattern
        * state.py -- state machine pattern
    + sample.py -- handling the given samples
    + sketch.py -- wrapper for Sketch
    + util.py -- utility functions
- template/ -- templates
    + android/ -- Android platform modelings
    + app/ -- templates for applications
        * android/ -- Android apps
            - hierarchy.py -- a script to extract class hierarchy from the apk
        * gui/ -- Java GUI apps
    + gui/ -- Swing modelings
    + pattern/ -- templates for examples in example/gui/src/pattern/ folder

