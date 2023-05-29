# Bedrock 2
Bedrock is a foundation for rapidly building microservices with client interactions. Version 2 is a re-architecture that moves all parameters into the POST data for security.

## Requirements
* mongodb
* docker
* gcc
* gnupg2
* node
    * yuidoc
    * uglifyjs

## Homebrew on MacOS
Use brew and then npm to install required components:
```
xcode-select --install
brew install git openjdk maven ant tomcat node gcc gnupg2
brew install --cask docker
brew tap mongodb/brew; brew update; brew install mongodb-community
npm -g install uglify-js
npm -g install yuidocjs
```

### Silicon
Use brew to configure the environment from your shell profile (as opposed to just adding brew to the path). We stubbornly use the bash shell to capitalize on years of bash scripts on many versions of linux, cygwin on PCs, and (previously) bash on MacOS. 

On Apple silicon (M1, M2, etc.):
```
eval "$(/opt/homebrew/bin/brew shellenv)"
```

On Intel silicon:
```
eval "$(/usr/local/bin/brew shellenv)"
```

The install location of homebrew is different between Intel silicon and Apple Silicon (/usr/local vs /opt/homebrew). Using the environment variable $HOMEBREW_PREFIX created by the shellenv action can help to keep scripts portable.

Another way to deal with the difference is to symlink /usr/local to /opt/homebrew on Intel silicon:

```
sudo ln -s /usr/local /opt/homebrew
```

### Versions

Working with the brew installations can be troublesome when updates happen and your version disappears. The complete installations are maintained in the `$HOMEBREW_PREFIX/Cellar` directory, but best-practice is to use the symlinks in the `$HOMEBREW_PREFIX/opt` directory, so you don't have to hard code a version. 

There will typically be links to the Cellar named with and without a version number. For instance:

```
tomcat      -> ../Cellar/tomcat/10.1.9
tomcat@10   -> ../Cellar/tomcat/10.1.9
```

### Java
For Java, most applications expect the JDK to be in `/Library/Java/JavaVirtualMachines/`, but brew won't automatically do this. Add a link manually like this:

```
pushd /Library/Java/JavaVirtualMachines/
sudo ln -s $HOMEBREW_PREFIX/opt/openjdk/libexec/openjdk.jdk openjdk
```

### Tomcat
The brew version of tomcat remaps the standard error stream in the service. If you want tomcat to put logs in the "normal" place, change the tomcat configuration plist in:
```
$HOMEBREW_PREFIX/opt/tomcat/homebrew.mxcl.tomcat.plist
```

Add the following lines inside the `<dict>` node (order is important, and each pair is two keys in sequence):
```
<key>StandardOutputPath</key>
<string>/opt/homebrew/opt/tomcat/libexec/logs/catalina.out</string>
<key>StandardErrorPath</key>
<string>/opt/homebrew/opt/tomcat/libexec/logs/catalina.out</string>
```
Remember the value of $HOMEBREW_PREFIX is different on Intel silicon from Apple Silicon, so make sure you set the path in the plist to match your installation.

Restart the tomcat service to implement the change:

```
brew services restart tomcat
```

Once this is done, you can track the log using tail:

```
tail -f $HOMEBREW_PREFIX/opt/tomcat/libexec/logs/catalina.out
```
