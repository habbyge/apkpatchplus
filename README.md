# apkpatchplus
基于apkpatch工具，生成patch包：
1. 支持多dex、支持新增class、field、method
2. 配合[iWatch](https://github.com/habbyge/iWatch)补丁项目(支持Android5.0~11.x)使用，能够在Native(C/C++)层支持补丁修复，包括：新增class、field、method；修改method等

# 使用方案
1. 环境：Mac系统（windows类似）
2. 软件：MacZip用于打开jar包，并展示其中的class文件，此软件可以导入修改后的class文件，并覆盖掉旧的；其实还可以新增、删除等操作，总之就是编辑jar包用的。
3. 通过itellij idea新建一个工程，并导入目标jar包，查看jar包中的class文件，找到需要或修改或新增或删除的class文件，然后在src/中新建同样包路径下的需要修改的目标class文件，修改后，记得recompile目标文件：
4. 再通过MacZip导入修改后的class文件到目标jar包中的同名的class文件，覆盖替换之，即可。
