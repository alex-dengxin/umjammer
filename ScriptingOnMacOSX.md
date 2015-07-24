# Scripting on Mac OS X #

## Japanese Settings ##

| command | disable readline (for rlwrap) | enable Japanese | i18n | comment |
|:--------|:------------------------------|:----------------|:-----|:--------|
| ipy     | NG                            |                 | OK   |         |
| python  | NG?                           |                 | OK   | apple NG |
| irb     | see below                     |                 | OK   |         |
| gosh    | n/a                           |                 | OK   |         |
|         |                               |                 |      |         |
| scala   | -Xnojline                     |                 | OK   |         |
| groovysh | --terminal=none               | -Dfile.encoding=utf-8 | NG   | need to modify source |
| jython  | export JYTHON\_OPTS=-Dpython.console=org.python.util.InteractiveConsole |                 | OK   |         |
| jirb    | --noreadline                  | -Ku             | OK   |         |
| clj     | n/a                           | -Dfile.encoding=utf-8 | OK   |         |
| sisc    | n/a?                          | -Dfile.encoding=utf-8 | OK   | see below |
| abcl    | n/a?                          | -Dfile.encoding=utf-8 | OK   |         |
| tuprolog | n/a?                          | -Dfile.encoding=utf-8 | OK   |         |
| beansh  | n/a?                          | -Dfile.encoding=utf-8 |	OK   |         |
| rhino   | n/a?                          | -Dfile.encoding=utf-8 | OK   |         |
| pnuts   | n/a?                          | -Dfile.encoding=utf-8 | OK   |         |

  * sisc
```
sisc.reader.CharUtil#charToEscapedIfNecessary
diff -r1.9 CharUtil.java
100c100,101
<             if (c < ' ' || c > '~') return "u"+charToHex(c);
---
> //            if (c < ' ' || c > '~') return "u"+charToHex(c);
>             if (c < ' ') return "u"+charToHex(c);
```
  * irb
```
~/.irbrc
IRB.conf[:USE_READLINE] = false
```