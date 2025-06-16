#OpenMP #MPI
## OpenMp简介

### 体系结构
 **组成**
- 编译器指令
- 运行时库函数
- 环境变量
**支持以增量的方式并行化串行程序**
**基于线程**
**需要编译器支持**

**Fork-Join模型**
程序开始于一个主线程，主线程执行直到遇到第一个并行区域。
Fork：主线程创建一组并行线程，并行区域中这组线程并行执行
Join：并行区域结束后，线程同步并终止。

**编译制导**

```C++
#include <omp.h> //包含openmp库函数的头文件

#pragma omp parallel private(nthreads, tid)
pragma opm 是制导指令前缀

parallel是制导指令，

子句
```
- 大小写敏感
- 每个指令最多应用于一个后继语句，语句必须是一个结构块（structured block）


### 并行区域结构
``` C
#pragma omp parallel [[clause[[,]clause]…]
    structured-block
    
clause:
    if(scalar-expression)
    num_threads(integer-expression)
    default(shared | none)
    private(list)
    firstprivate(list)
    shared(list)
    copyin(list)
    reduction(reduction-identifier: list)
```
♦并行区域是多线程执行的代码块，是基本的OpenMP并行结构
♦一个线程执行到一个并行指令时，Fork一组线程并成为该线程组的主线程（线程号为0）
♦从并行区域开始，代码被复制，一组线程都执行该代码
♦并行区域出口隐含barrier，只有主线程继续执行

#### OpenMP 指令作用域

**静态范围**
指 **编译制导语句（OpenMP 指令）直接作用的文本代码范围**，即指令之后被明确封装在一个结构块内的代码区域。
- 作用域严格限定在 **当前程序单元（函数或代码块）内**，**不能跨越函数边界或文件**。
- 指令直接嵌套在并行块（`parallel`）或循环块（`for`/`do`）中。
```c
void fun() {
    #pragma omp parallel  // 并行区域开始
    {
        #pragma omp for  // do 指令嵌套在 parallel 块内，属于静态范围
        for (int i=0; i<N; i++) {
            // 并行循环代码
        }
    }  // 并行区域结束
}
```
- 此处 `#pragma omp for` 的作用域仅限于 `parallel` 块内的循环，属于 **静态范围指令**。

**孤立指令**
指 **不依赖于其他 OpenMP 语句的独立编译制导语句**，其作用域**可以跨越函数或文件边界**。
- 常见指令：`critical`、`section`、`atomic`、`flush` 等。
- 特点：这些指令的作用域不局限于当前代码块，而是与程序的动态执行流程相关。
```c
void sub1() {
    #pragma omp critical  // 孤立指令，作用域可跨函数
    {
        // 临界区代码
    }
}

void fun() {
    #pragma omp parallel
    {
        sub1();  // 在并行块中调用 sub1，critical 指令作用域包含此调用
    }
}
```
- `critical` 指令在 `sub1` 中独立存在，当 `fun` 的并行块调用 `sub1` 时，`critical` 的作用域**动态扩展到并行块内**，属于孤立指令

**动态范围**
动态范围是 **静态范围和孤立指令作用域的并集**，指 **OpenMP 指令在程序动态执行过程中实际影响的所有区域**。
- 包含：
    1. **静态范围**：指令直接作用的代码块（如并行块内的嵌套指令）。
    2. **孤立指令的作用域**：跨函数调用的独立指令（如 `critical`、`section`）。
- 核心逻辑：指令的影响范围随程序执行流程（如函数调用）动态扩展。
```c
void sub1() {
    #pragma omp section  // 孤立指令
    {
        // 代码段 1
    }
}

void sub2() {
    #pragma omp section  // 孤立指令
    {
        // 代码段 2
    }
}

void fun() {
    #pragma omp parallel
    {
        #pragma omp sections  // 静态范围：parallel 块内的 sections 指令
        {
            sub1();  // 调用 sub1，section 指令作用域扩展至此
            sub2();  // 调用 sub2，section 指令作用域扩展至此
        }
    }
}
```
- `sections` 指令的静态范围是 `parallel` 块内的 `sections` 结构。
- 但 `sub1` 和 `sub2` 中的 `section` 指令作为孤立指令，其作用域**动态加入到 parallel 块的执行流程中**，因此整个 `parallel` 块及被调用函数均属于动态范围。

**注意事项**

1. **作用域嵌套规则**：
    - 内层指令的作用域被外层指令包裹（如 `for` 在 `parallel` 块内，属于并行区域的一部分）。
    - 孤立指令的作用域需通过动态执行路径显式关联（如函数调用）。
2. **线程安全与资源竞争**：
    - 孤立指令（如 `critical`）需确保跨函数调用时的线程安全，避免多个线程同时访问共享资源。
    - 动态范围可能导致隐性的作用域扩展，调试时需关注指令的实际影响范围。
3. **性能优化**：
    - 静态范围指令适合局部并行优化（如循环并行）。
    - 孤立指令和动态范围适用于跨模块的同步控制（如全局临界资源保护）。

#### OpenMP 数据作用域

OpenMP 采用**共享内存模型**，默认情况下：
- **大多数变量是共享的**（如全局变量、静态变量），所有线程可访问同一内存地址。
- **部分变量默认是私有的**，每个线程拥有独立副本。

 **默认共享的变量**
1. **全局变量**：程序中所有函数均可访问的变量。
2. **静态变量**（`static`修饰）：包括函数内的静态局部变量。
3. **并行区域外定义的变量**（除非显式声明为私有）。

 **默认私有的变量**
1. **循环索引变量**（`for`/`do`循环中的控制变量）。
2. **并行区域调用的子程序中的堆栈变量**（函数内局部变量）。


OpenMP 提供**数据共享属性子句**来显式控制变量的作用域，常用子句包括：

- `shared(var)`：指定变量为共享变量。
- `private(var)`：指定变量为私有变量（每个线程独立副本）。
- `firstprivate(var)`：私有变量，初始值来自主线程。
- `lastprivate(var)`：将最后一个迭代的值复制回主线程变量。
- `reduction(operator:var)`：对私有变量进行归约操作（如`+`、`*`、`max`等）。
```c
#include <stdio.h>
#include <omp.h>

int global_var = 10;  // 全局变量，默认共享

void main() {
    int shared_var = 20;  // 并行区域外定义，默认共享
    int private_var = 30; // 并行区域外定义，默认共享
    
    #pragma omp parallel shared(shared_var) private(private_var)
    {
        int stack_var = 40;  // 并行区域内定义，默认私有（堆栈变量）
        
        // 修改共享变量（所有线程可见）
        shared_var += omp_get_thread_num();
        
        // 修改私有变量（仅当前线程可见）
        private_var += omp_get_thread_num();
        
        // 全局变量默认共享
        global_var += omp_get_thread_num();
        
        printf("Thread %d: shared_var=%d, private_var=%d, global_var=%d\n",
               omp_get_thread_num(), shared_var, private_var, global_var);
    }
    
    printf("After parallel region: shared_var=%d, private_var=%d, global_var=%d\n",
           shared_var, private_var, global_var);
}
```
结果：
```plaintext
Thread 0: shared_var=20, private_var=30, global_var=10
Thread 1: shared_var=21, private_var=31, global_var=11
After parallel region: shared_var=21, private_var=30, global_var=11
```
1. shared_var
所有线程共享同一个内存地址
累加了0和1，结果是21
2. private_var
**作用域**：每个线程拥有独立副本，主线程的初始值 30 仅用于初始化线程私有变量。
线程0将私有副本加0
线程1将私有副本加1
并行区域结束后，==**私有变量的副本不会同步回主线程**==，因此主线程的`private_var`仍为初始值 30。
3. global_var
类似于shared_var


**关键数据作用域规则**
1. **共享变量的风险**：
    - 多个线程同时修改共享变量可能导致**竞态条件**（Race Condition）。
    - 需使用同步机制（如`critical`、`atomic`）保护共享资源。
2. **私有变量的特性**
    - 私有变量在并行区域开始时初始化（若未指定`firstprivate`，初始值未定义）。
    - 并行区域结束后，主线程中的私有变量值**不会被更新**（除非使用`lastprivate`）。
3. **循环索引变量的特殊性**：
	 - 循环索引变量（如`i`）默认是私有的，每个线程独立执行不同的迭代。


**高级数据作用域控制**
 **1. firstprivate 与 lastprivate**
```c
int x = 10;

#pragma omp parallel firstprivate(x)//对线程的初始值进行规定
{
    x += omp_get_thread_num();  // 每个线程的 x 初始值为 10
    printf("Thread %d: x=%d\n", omp_get_thread_num(), x);
}

#pragma omp parallel for lastprivate(x)//对主线程的值作用
for (int i=0; i<10; i++) {
    x = i;  // 最后一个迭代的值（i=9）会被赋给主线程的 x
}
```
 **2. reduction 子句**
 ```c
int sum = 0;

#pragma omp parallel for reduction(+:sum)
for (int i=0; i<1000; i++) {
    sum += i;  // 每个线程计算部分和，最终合并到主线程的 sum
}
```

#### 用于显示定义变量范围的子句
private(list)：将list中的变量声明为每个线程的私有变量
	为线程组中每个线程声明一个相同类型的变量
	作用域只在并行区域内

shared(list)：将list中的变量声明为线程组中线程之间的共享变量

default(shared | none)：指定并行区域内变量的属性是shared，none作为默认值要求程序员必须显式地限定所有变量的作用域

firstprivate(list)：在进入并行区域之前进行一次初始化，让并行区域的list中变量的值初始化为同名共享变量的值

lastprivate(list)：在退出并行区域时，将并行区域的list中变量的值赋值给同名的共享变量
	循环迭代：将最后一次循环迭代中的值赋给对应的共享变量
	section结构：将语法上最后一个section语句中的值赋给对应的共享变量

copyin(list)：将主线程中threadprivate变量的值拷贝到执行并行区域的各个线程的threadprivate变量中，list包含要复制的变量的名称
	threadprivate是指令，指定全局变量被所有线程各自产生一个私有的副本，对于不同并行区域之间的同一个线程，该副本变量是共享的

copyprivate(list)：将单个线程私有list变量的值广播到其他线程的私有list变量
	只用于single指令，在一个single块的结尾处完成广播操作

reduction(reduction-identifier: list) ：对list中的变量进行约简操作
	为每个线程创建并初始化list中变量的私有副本（list中变量为共享变量）
	对所有线程的私有副本进行约简操作，并将最终结果写入共享变量
	约简操作符和初始值

if 子句： 如果有if子句，那么只有表达式为真（非0）才会创建一个线程组，否则该区域由主线程串行执行。

num_threads(integer-expression)：用于设置运行并行区域的线程数量，integer-expression表示线程数量。
并行区域的线程数量由以下因素决定，优先级从高到低：
1. if子句的结果
2. num_threads子句的设置
3. omp_set_num_threads()库函数的设置
4. OMP_NUM_THREADS环境变量的设置
5. 编译器默认实现（一般默认总线程数等于处理器的核心数）

**使用限制**
♦并行区域不能是跨越多个程序或代码文件的结构化块
♦从一个并行区域只能有一个入口和一个出口，任何转入和转出都是非法的
- 不能包含break语句
♦只允许一个if子句和一个num_threads子句
♦程序运行结果不能依赖于子句的顺序


### 共享任务结构Worksharing Constructs
- 不会启动新线程，将工作共享结构封装在一个并行区域中
- 工作共享结构将所包含的代码划分给线程组的成员来执行
- 出口隐含barrier
	并行do/for loop
	并行sections
	single：执行串行代码

#### for指令：
for指令指定循环语句必须由线程组并行执行，假设已经启动了并行区域，否则将串行执行

**格式**
```c
#pragma omp for [[clause[[,]clause]…]
    for-loops
clause:
    schedule(kind[,chunk_size])
    ordered[ (n) ]
    private(list)
    firstprivate(list)
    lastprivate(list)
    reduction(reduction-identifier : list)
    collapse(n)
    nowait
```

**schedule子句**
schedule(kind[,chunk_size]) 描述循环迭代如何划分给多个线程
kind可以为
	static（静态）：循环迭代被划分成小块，静态的分配给线程。
	dynamic（动态）：循环迭代被划分成小块，在线程间动态调度。
	guided（引导）：
	当线程请求任务时，迭代块被动态分配给线程，直到所有迭代块被分配完为止。与dynamic类似，只是每次分配的迭代块的大小会减少。
		初始块大小与num_iterations/num_threads成正比
		后续分配的块大小与num_iterations_remain/num_threads成正比
		chunk_size定义最小块大小，默认为1
	runtime（运行时）：运行时根据环境变量omp_schedule在确定调度类型，最终使用的是上述三种之一
	auto（自动）:由编译器或者运行时系统决定调度类型


**ordered子句和nowait子句**
ordered[ (n) ]：指定区域的循环迭代将按串行顺序执行，与单个处理器处理结果顺序一致
	ordered子句只能用在for或parallel for中

nowait：忽略并行区域隐含barrier的同步等待


**collapse子句**
collapse(n)：指定嵌套循环中的n个循环折叠到一个大的迭代空间中，并根据调度子句划分并行执行
- 所有相关循环的顺序执行决定了折叠迭代空间中迭代的顺序
- 能够解决线程间负载均衡或线程负载太小的问题

**使用限制**
不能是while循环，或者任何循环迭代次数不确定的循环
循环迭代变量（i）必须是整数，并且对于所有线程，循环控制参数（i++）必须相同
程序的正确性不能依赖于某个线程执行的特定迭代
跳转或跳出循环是非法的
块大小必须为整数次迭代
schedule、ordered和collapse子句只可以出现一次


#### Sections指令
sections指令指定所包含的代码段被分配给各个线程执行
不同section部分可以由不同线程执行，如果一个线程运行的块，也可以执行多个部分
```c
#pragma omp sections [clause[ [, ] clause] ...]
{
    #pragma omp section
        structured-block
    #pragma omp section
        structured-block
    ...
}

clause:
    private(list)
    firstprivate(list)
    lastprivate(list)
    reduction(reduction-identifier: list)
    nowait
```
**使用限制**
sections指令的末尾有隐含的barrier，可以使用nowait子句忽略
不能跳转或跳出section代码块
section指令必须在封闭的sections指令的词法范围内


#### single指令
指令指定所包含的代码仅由一个线程执行，通常用于处理非线程安全的代码段，例如I/O
```c
#pragma omp single [clause[ [, ]clause] ...]
    structured-block
clause:
    private(list)
    firstprivate(list)
    copyprivate(list)
    nowait
```
**使用限制**
不能跳转或跳出一个single代码块

#### 合并结构Combined Constructs
- 合并了并行区域结构与共享任务结构的指令
- parallel for指令：合并parallel和for两个指令
	除了nowait子句外，所有parallel和for适用的子句和规范也都适用于parallel for指令
- parallel sections指令：合并parallel和sections两个指令
	除了nowait子句外，所有parallel和sections适用的子句和规范也都适用于该指令


### 同步结构
实现多线程间互斥访问和同步的指令

**master指令** 指定一个代码区域由主线程执行，其他线程跳过这个区域
```c
#include <omp.h>
main()
{
    int x = 0;
    #pragma omp parallel shared(x)
        {
            #pragma omp master
    x = x + 10;
            #pragma omp critical
                  x = x + 1;
        }  /* end of parallel section */
    printf("out of the parallel region : X = %d\n",x);
}
```
没有隐含barrier指令
跳出或者转出是违法的

**critical指令** 指定一个代码区域每次只能由一个线程执行（互斥访问），可以实现临界区访问
- 如果一条线程正在一个critical区域执行而另一个线程到达这个区域，并企图执行，那么它将会被阻塞，直到第一个线程离开这个区域
- name是可选项，使不同的cirtical区域共存，具有相同命名的不同的critical区域被当作同一个区域，所有未命名critical区域被当作同一个区域

**barrier指令** 指定线程组所有的线程在此指令处同步

**atomic指令** 指定以原子方式访问特定的存储位置，该指令仅适用于其后的单个语句，可以实现一个最小临界区的访问

**flush指令** 标识一个数据同步点，将线程的变量写回内存，实现内存数据更新
- 用于明确的表明程序点处需要进行内存更新
- 某些指令隐含flush指令，不过如果有nowait子句，则flush指令失效：
	barrier指令；
	parallel指令——进入和退出
	critical指令——进入和退出
	ordered指令——进入和退出
	for指令——退出
	sections指令——退出
	single指令——退出

**ordered指令** 指定循环迭代以串行执行顺序执行
- 指定循环迭代以串行执行顺序执行，如果前面的迭代没有完成，则执行后面迭代的线程需要等待。
- ordered指令只能出现在出现在for或者parallel for的动态范围内

**threadprivate指令**
指定全局变量被所有线程各自产生一个私有的副本，对于不同并行区域之间的同一个线程，该副本变量是共享的

### 库函数
omp_get_thread_num()
omp_get_num_threads()
![[Pasted image 20250517142028.png]]
![[Pasted image 20250517142047.png]]

### 环境变量
![[Pasted image 20250517142101.png]]


