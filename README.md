# SSHDroid

给android app提供的一个sshd服务，起主要作用是提供一个交互式的shell工具，
我们可以通过他访问app的内部数据文件。

**请注意本服务和android原生adb是不冲突的，因为adb无法访问app私有文件目录，SSHDroid则通过插件注入的方式将私有文件系统暴露出来**


## 运行

1. 复制文件: ``app/src/main/assets/config.template.properties``-> `` app/src/main/assets/config.properties``
2. 修改内容
    - targetPackage: 需要被注入的进程
    - ssdServerPort: 启动的端口号，请注意不要和其他服务冲突
    - newProcess: 是否需要在新进程中启动,如果你的app开启了分身，那么最好将这个配置设置为true。因为分身功能将会破坏原有文件系统(特别是平头哥系统)
3. 安装并运行插件
4. 在电脑上面运行adb forward,如： ``adb forward tcp:3478 tcp:3478``
5. 使用电脑连接服务，如：
    - ssh 127.0.0.1 -p 3478 登陆shell
    - scp -P 3478 ./test.txt 127.0.0.1:/data/data/xxx/files/
    - scp -P 3478 127.0.0.1:/data/data/xxx/files/aaa.log ~/Desktop/

## 自动化和集群化
请基于当前工具做二次封装

