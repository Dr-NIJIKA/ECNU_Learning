
### Hello World！
Pthreads 不是编程语言，而是POSIX 线程库，也经常称为Pthreads 线程库
Pthreads线程库支持：
- 创建并行环境
- 同步
- 隐式通信

```C
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

int thread_count;  /*全局变量*/
void* Hello(void* rank);  /*线程运行的函数*/
int main(int argc, char* argv[]){

    long thread; /*为了适应64位系统*/
    pthread_t* thread_handles;
    thread_count = strtol(argv[1],NULL,10); /*从命令行获得线程数量*/
    thread_handles = malloc(thread_count*sizeof(pthread_t)); /*分配存储空间*/
    for(thread = 0; thread < thread_count; thread++)
        pthread_create(&thread_handles[thread],NULL,Hello, (void*) thread);
    printf("Hello from the main thread\n");
    for(thread = 0; thread < thread_count; thread++)
        pthread_join(thread_handles[thread], NULL);
    free(thread_handles);
    return 0;
}

void* Hello(void* rank) {
    long my_rank = (long) rank
    printf("Hello from thread %ld of %d\n", my_rank, thread_count);
    return NULL;

}
```

### 临界区
1. 使用标志变量
```C
While(flag!=my_rank);//标志还没到时，不会运行下面的求和
sum += factor / (2 * i +1);
flag++;
```
1. busy waiting
	♦空循环造成了性能的降低
	♦线程有序的执行临界区
	♦临界区的互斥执行降低了代码的并行性，减少临界区执行次数的方法：
		每个线程配置私有变量my_sum存储各自的计算结果
2. 互斥锁
	互斥锁数据类型：pthread_mutex_t==互斥锁可以用来限制每次只有一个线程能进入临界区==
	需要初始化和回收空间
```C

  int pthread_mutex_init(
         pthread_mutex_t*  mutex_p,
         const pthread _mutexattr_t*   attr_p);


 int pthread_mutex_destroy(pthread_mutex_t*   mutex_p) ;


 int pthread_mutex_lock(pthread_mutex_t*   mutex_p);//线程获得锁
 
 int pthread_mutex_unlock(pthread_mutex_t*  mutex_p);//线程释放锁

 ```

3. 信号量Semaphores（PV操作）
一种特殊类型的unsigned int 变量；与互斥量最大的区别在于信号量没有个体拥有权，主线程信号量初始化，所有线程都可以通过调用sem_post 和sem_wait 函数更新信号量的值
==#include <semaphore.h>==
![[Pasted image 20250602185047.png]]

```C
sem_t sem;/*全局变量*/

/*主函数*/

 sem_init(&sem, 0, 1);/*信号量初始值为1*/

/*多线程并行执行函数部分*/       

sem_wait(&sem);

sum+=my_sum;

sem_post(&sem);

/*主函数*/

sem_destroy(&sem);/*回收信号量空间*/
```
### 同步
- 为保证所有my_sum都计算结束后才进行sum的计算，需要加入同步语句。
1. 忙等待和互斥锁实现路障
2. 信号量实现路障
3. 条件变量
```C
int counter = 0;

pthread_mutex_t mutex_p;

pthread_cond_t cond_p;

/*多线程并行执行函数部分*/

/*路障*/

pthread_mutex_lock(&mutex_p);

counter++;

If (counter == thread_count){

  counter = 0;

  pthread_cond_broadcast(&cond_p);

}else{

  while(pthread_cond_wait(&cond_p,&mutex_p) != 0);

}

pthread_mutex_unlock(&mutex_p);
```