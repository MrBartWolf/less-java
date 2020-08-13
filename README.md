[![Build Status](https://travis-ci.org/JMU-CS/less-java.svg?branch=master)](https://travis-ci.org/JMU-CS/less-java)

# Less-Java

A procedural programming language with a simple and concise syntax, implicit
but strong typing via type inference, and built-in unit testing.

## Getting Started

These instructions will get you a copy of the project up and running on your
local machine for development and testing purposes.

### Building The Compiler

Start out by building the compiler using Gradle. This project makes use of the
Gradle wrapper so installing Gradle is not necessary.

```
./gradlew build
````
### Compiling and Running a Less-Java Program

A script is provided to compile, run, and test any Less-Java program with a single command.
```
./lj <file-name>
````

For example,

```
./lj tests/fact.lj
```

You will need to make sure that your program has a `main()` method if you want to
run it. If it does not have a `main()` method, you will receive a warning similar
to the following.

```
Warning: This program does not contain a main() function and will not be run
```

The program will still be compiled and tested correctly even without a main() method, but no program output
will be released.

## Testing

### Running All Tests

It is possible to run all the tests in the `tests/` directory and to set the
expected output for any of those tests.

### Setting expected output

To set the expected output to the output from a particular execution of the
program, run the following:

```
./dotests.sh -s <file-name>
```

This will update or set the output for that program to the output from that
execution. This can be used to change the expected output for programs that
take input and then displays the input (or uses it in a way that changes the
output).


### Running all sample tests against expected outputs

To run all sample programs in the `tests/` directory against their expected
corresponding outputs in `tests/outputs/`, you can run the following:

```
./dotests.sh -r tests
```
## Distributing

Less-Java can be distributed by running `./distro.sh`. The files to distribute
will be located in the `distribution/` directory.

## Contributing

Contributions are welcome. Issues with the 'help wanted' tag that are not
assigned are open to be worked on. If you have ideas or suggestions that are
not already issues, please create an issue to discuss the feature before
beginning work. Please open a pull request to have your changes merged.

For information on working on this project as part of an honors thesis, reach
out to Dr. Lam.

## Authors

 - **Zamua Nasrawt** - [@Zamua](https://github.com/Zamua)
 - **Dr. Michael Lam** - [@lam2mo](https://github.com/lam2mo)

## License

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation, version 3.

For more information, see the [LICENSE](LICENSE) file.

## Academic Citations
If you use this project in an academic project, please cite the following paper:

Paper: Zamua O. Nasrawt and Michael O. Lam. 2019. Less-Java, more learning: language design for introductory programming. J. Comput. Sci. Coll. 34, 3 (January 2019), 64-72. [ACM DL](https://dl.acm.org/citation.cfm?id=3306476)
