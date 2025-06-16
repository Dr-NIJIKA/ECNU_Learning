#MPI 

## MPI简介
### 分布式内存系统
♦每个处理器有私有内存，处理器只能访问自己的内存
♦运行在处理器-内存对上的程序称为进程
♦进程间采用显式的==消息传递==进行通信：一个进程调用消息==发送函数==，另一个进程调用消息==接收函数==

### MPI
Message-Passing Interface , MPI for short;
♦MPI是一个消息传递接口标准，而不是编程语言
♦MPI标准定义了一组具有可移植性的编程接口
♦MPI以语言独立的形式存在，可运行在不同的操作系统和硬件平台上

## 简单MPI程序
```C
#include "mpi.h" /*MPI头函数，提供了MPI函数和数据类型定义*/

int main( int argc, char** argv )
{
int rank, size, tag=1;
int senddata,recvdata;
MPI_Status status;
MPI_Init(&argc, &argv); /*MPI的初始化函数*/
MPI_Comm_rank(MPI_COMM_WORLD, &rank); /*进程编号*/
MPI_Comm_size(MPI_COMM_WORLD, &size); /*总进程数*/

if (rank==0){
senddata=9999;
MPI_Send( &senddata, 1, MPI_INT, 1, tag, MPI_COMM_WORLD); /*发送数据到进程1*/
}

if (rank==1)
MPI_Recv(&recvdata, 1, MPI_INT, 0, tag, MPI_COMM_WORLD, &status);
/*从进程0接收数据*/
MPI_Finalize(); /*MPI的结束函数*/

return (0);

}
```

## MPI消息

### 消息
由 **消息缓冲** 和 **消息信封** 组成

- 消息缓冲由三元组<起始地址，数据个数，数据类型>标识
- 消息信封由三元组<源/目标进程，消息标签，通信域>标识

**起始地址** 数据个数 数据类型 源/目标进程 标签 通信域
起始地址 是一个缓冲区（？）
### 数据类型
**预定义类型**和**派生数据类型**
- 预定义数据类型：支持异构计算
- 派生数据类型：MPI引入派生数据类型来定义由数据类型不同且地址空间不连续的数据项组成的消息
#### 预定义类型：
除去基本的
MPI_INT MPI_DOUBLE MPI_CHAR MPI_FLOAT……
还有附加类型：
♦MPI_BYTE：表示一个字节，所有的计算系统中一个字节都代表8个二进制位
♦MPI_PACKED：预定义数据类型被用来实现传输地址空间不连续的数据项
![[Pasted image 20250612154110.png]]
首先 调用MPI_PACK_size函数，然后分配内存，
调用MPI_PACK打包，之后MPI_SEND
MPI_PACK(buf, count, type,  packbuf,  packsize, packpos ,com)

#### 派生数据类型：
派生数据类型可以用类型图来描述，这是一种通用的类型描述方法，它是一系列二元组<基类型，偏移>的集合，可以表示成如下格式：
- {<基类型0,偏移0>，···，<基类型n-1,偏移n-1>}
基类型可以是任何MPI预定义的数据类型，也可以是其他的派生数据类型（支持数据类型的嵌套定义）
![[Pasted image 20250612155709.png]]
```C
MPI_Datatype newtype;
int matrix[6][10]; // 6行10列矩阵

// 创建一个数据类型，描述矩阵的第1,3,5列(每列6个元素)
MPI_Type_vector(3, 6, 2*10, MPI_INT, &newtype);
MPI_Type_commit(&newtype);

// 现在可以使用newtype来发送/接收这些列数据
vector 分别表示 共有3个块，每块的大小，相邻块间隔，原类型，新类型
```

```C
MPI_Type_struct(
    count, //成员数
    array_of_blocklengths, //成员块长度数组
    array_of_displacements,//成员偏移数组    
    array_of_types, //成员类型数组
    newtype // 新类型
)
```
如何获取偏移数组：
MPI_Aint addr1,addr2,addr3;（MPI标准数据类型，用于存放整形地址）
int a,b,c,dist1,dist2;

MPI_Address(&a,&addr1);（获得地址赋给a）
MPI_Address(&b,&addr2);
MPI_Address(&c,&addr3);

dist1=addr2-addr1;（相减得到偏移量）
dist2=addr3-addr2;

### 消息标签
发送者连续发送两个相同类型消息给同一个接收者，如果没有消息标签，接收者将无法区分这两个消息
消息标签使得服务进程可以对两个不同的用户进程分别处理，提高灵活性
MPI_ANY_TAG：通配符，任何tag都可以匹配

### 通信域
通信域包括**进程组(Process Group)** 和**通信上下文(Communication Context)** 等内容，用于描述通信进程间的通信关系。
分为组内通信和组间通信
- 进程组：是进程的有限有序集。
	进程的个数n是有限的 ==MPI_Comm_size(communicator, &group_size)==
	进程是按照0.1.2...的顺序排列的。==MPI_Comm_rank(communicator, &my_rank)==
- 通信上下文：安全的区别不同的通信以免相互干扰，并非显式的对象，只是通信域的一部分。
- ==MPI_COMM_WORLD是所有进程的集合==

一些关于通信域的函数
```C
MPI_Comm MyWorld, SplitWorld;
int my_rank, group_size, Color, Key;

MPI_Init(&argc, &argv);

MPI_Comm_dup(MPI_COMM_WORLD,&MyWorld);//复制一个已有的通信域生成一个新的通信域，两者除通信上下文不同外，其它都一样。
MPI_Comm_rank(MyWorld,&my_rank);
MPI_Comm_size(MyWorld,&group_size);

Color=my_rank%3;

Key=my_rank/3;
MPI_Comm_split(MyWorld,Color,Key,&SplitWorld);//从一个指定通信域分裂出多个子通信域，每个子通信域中的进程都是原通信域中的进程
```

MPI_Comm_dup(MPI_COMM_WORLD,&MyWorld);这里讲原有的MPI_COMM_WORLDcopy给了MyWorld 具有相同的进程，但是上下文不同。

MPI_Comm_split(MyWorld,Color,Key,&SplitWorld) 在MyWorld的基础上产生了几个分割的子通信域。color用于划分通信域，key用于表示不同通信域中进程的编号
![[Pasted image 20250612185234.png]]
#### 组间通信域
是一种特殊的通信域，该通信域包括了两个进程组，分属于两个进程组的进程之间通过组间通信域实现通信。
有一些函数

### 消息状态（MPI_Status类型）
包括：
- 消息的源进程标识－－MPI_SOURCE
- 消息标签－－MPI_TAG
- 错误状态－－MPI_ERROR
- 其他－－包括数据项个数等，但多为系统保留的。
这是MPI_Recv的最后一个参数
```C
while (true){
MPI_Recv(received_request,100,MPI_BYTE,MPI_Any_source,MPI_Any_tag,comm,&Status);

switch (Status.MPI_Tag) {
	case tag_0: perform service type0;
	case tag_1: perform service type1;
	case tag_2: perform service type2;
}
```

## 点对点通信
MPI的点对点通信(Point-to-Point Communication )同时支持多种通信模式
（指的是缓冲管理，以及发送方和接收方之间的同步方式）
- 同步通信模式
只有相应的接收过程已经启动，发送过程才正确返回。

- 缓冲通信模式
缓冲通信模式的发送不管接收操作是否已经启动都可以执行。
但是需要用户程序事先申请一块足够大的缓冲区，通过MPI_Buffer_attch实现，通过MPI_Buffer_detach来回收申请的缓冲区。

- 标准通信模式
是否对发送的数据进行缓冲由MPI的实现来决定，而不是由用户程序来控制。

- 就绪通信模式
发送操作只有在接收进程相应的接收操作已经开始才进行发送
♦接收操作必须先于发送操作启动。

还有两种通信机制：阻塞和非阻塞通信的主要区别在于==返回后的资源可用性==
- 阻塞
	- ==通信操作已经完成==，即消息已经发送或接收。
	- ==调用的缓冲区可用==。若是发送操作，则该缓冲区可以被其它的操作更新；若是接收操作，该缓冲区的数据已经完整，可以被正确引用。

- 非阻塞
	非阻塞通信返回后并不意味着通信操作的完成，MPI还提供了对**非阻塞通信完成的检测**，主要的有两种MPI_Wait函数和MPI_Test函数
![[Pasted image 20250612193605.png]]
在阻塞通信的情况下，通信还没有结束的时候，处理器只能等待，浪费了计算资源。

1. 一种常见的技术就是设法使计算与通信重叠，非阻塞通信可以用来实现这一目的
2. 双缓冲方法
![[Pasted image 20250612193816.png]]

#### Sendrecv函数
♦给一个进程发送消息，从另一个进程接收消息；
MPI_Sendrecv(
       sendbuf, sendcount, sendtype, dest, sendtag,
      //以上为消息发送的描述
      recvbuf, recvcount, recvtype, source, recvtag,
     // 以上为消息接收的描述
     comm, status)

## 集合通信
是一个进程组中的所有进程都参加的全局通信操作。
三个功能：通信 聚集 同步
三种集合通信：一对多 多对一 多对多

### 通信：
- 广播 Bcast
MPI_Bcast(Address, Count, Datatype, Root, Comm)
Root进程发送相同的消息给通信域Comm中的所有进程。

- 收集Gather
MPI_Gather(SendAddress, SendCount, SendDatatype, 
RecvAddress, RecvCount, RecvDatatype, 
Root, Comm)
Root 进程从Comm的所有进程中接受消息
消息按照rank排序并进行连接，放在Root的接受缓存中。

- 散播Scatter
–MPI_Scatter(SendAddress, SendCount, SendDatatype, 
RecvAddress, RecvCount, RecvDatatype, 
Root, Comm)
与Gather相反，Root进程给所有进程(包括它自已)发送不同的消息，这n (n为进程域comm包括的进程个数)个消息在Root进程的发送缓冲区中按进程标识的顺序有序地存放。

- 全局收集Allgather
MPI_Allgather(SendAddress, SendCount, SendDatatype, 
RecvAddress, RecvCount, RecvDatatype,
Comm)
相当与对每一个进程都当作一个root进程执行了一次gather。

- 全局交换Alltoall（对角线交换）
–MPI_Alltoall(SendAddress, SendCount, SendDatatype,
RecvAddress, RecvCount, RecvDatatype, 
Comm)
–全局交换等价于每个进程作为Root进程执行了一次散播操作。

### 同步：
MPI_Barrier（Comm）
- 在路障同步操作MPI_Barrier(Comm)中，通信域Comm中的所有进程相互同步。
- 在该操作调用返回后，可以保证组内所有的进程都已经执行完了调用之前的所有操作，可以开始该调用后的操作。
### 聚集：
♦集合通信的聚合功能使得MPI进行通信的同时完成一定的==计算==。
1. 首先通信的功能，即消息根据要求发送到目标进程，目标进程也已经收到了各自需要的消息
2. 对消息的处理，执行计算功能
3. 把处理结果放入指定的接受缓冲区

- 归约
MPI_Reduce(SendAddress, RecvAddress, Count, Datatype, Op, Root, Comm)
归约操作对每个进程的发送缓冲区(SendAddress)中的数据按给定的操作进行运算，并将最终结果存放在Root进程的接收缓冲区(RecvAddress)中。==向量的==
![[Pasted image 20250612201222.png]]

- 扫描
MPI_scan(SendAddress, RecvAddress, Count, Datatype, Op, Comm)
可以把扫描操作看作是一种特殊的归约，即每一个进程都对排在它前面的进程进行归约操作。
MPI_SCAN调用的结果是：对于每一个进程i，它对进程0,1,…,i的发送缓冲区的数据进行了指定的归约操作。
扫描操作也允许每个进程贡献==向量值==，而不只是标量值。向量的长度由Count定义。