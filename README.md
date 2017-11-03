# LikeViewDemo
仿写即刻app的点赞效果

> 作为忠实粉，先丢个凯哥的广告。[HenCoder](http://hencoder.com/)  
凯哥更有名的名字应该叫[扔物线](https://github.com/rengwuxian)

HenCoder真的是很良心的作品，拜服。

本项目github：[LikeViewDemo](https://github.com/lanzry/LikeViewDemo)

### 1. 准备工作
#### 1.1 tips
这篇文章是为响应henCoder第一期学习的末期活动——仿写酷UI而写。很遗憾，由于工作原因，当时没有写完，没有参加。后来写完之后，活动结果已经出来了，看了即刻app的Android组大佬精辟的点评之后，我又去重写了一遍代码。周期拖的太长了，还是先把这篇回顾写出来，请网上的各位大佬指教一下，顺便假如凯哥有时间，我也希望能投过去，交交作业。

#### 1.2 效果对比
即刻app原效果：

![](http://upload-images.jianshu.io/upload_images/2161259-fcb1b4d72f81d020.gif?imageMogr2/auto-orient/strip)

仿写效果：

![](http://upload-images.jianshu.io/upload_images/2161259-37921bf0c3078bd9.gif?imageMogr2/auto-orient/strip)  
自我点评：  
没有对比就没有伤害。不放在一起，觉得自己还算达到了效果，但是放在一起，发现还是有一些细节没有处理到位的。
1. 字体颜色、字体大小
2. 点赞和发光最后会弹一下，比较平滑。而我仅仅用了BounceInterpolator来处理，效果还是不符合，我认为这个应该需要自定义Interpolator来做到，而我恰好没做。
3. 仔细一看，发现数字变化的时候，其实还有间距、字体大小的变化。（即刻app上效果可能没有凯哥这张图这么明显，写回顾的时候才发现被我忽略了）

#### 1.3 获取资源
下载一个即刻app，没错。[即刻](https://www.ruguoapp.com/)
下载了apk之后，安装起来玩一玩，还蛮有意思的。
玩好之后，就把apk反编译了，apktool可以，直接解压一下也ok，到\res\drawable-hdpi-v4文件夹下，找到我们需要的资源：

![](http://upload-images.jianshu.io/upload_images/2161259-f0869f0d0a3b0163.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

作为没有玩过自定义的人来说，这个时候就想的有点多。比如“诶？三个png？这怎么玩呢？到底动画是在view上呢？还是drawable动画呢？”
最后动画做在onDraw()绘制里面。

#### 1.4 点赞接口
即刻的大佬的点评非常精辟，非常详细。看了之后觉得大佬做事非常认真严谨。

于是我也借鉴了一点里面的思想：把点赞抽象为接口，这样，假如需要更多的点赞自定义view，继承接口去实现会更好。

![](http://upload-images.jianshu.io/upload_images/2161259-f8af18cf0d21a46a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
我没考虑假如可以重复点赞的情况。

用接口的一个好处：

![](http://upload-images.jianshu.io/upload_images/2161259-561971cf5fe8555e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
把点赞相关都放在布局里面，通过布局的点击事件来统一触发点赞效果。

#### 1.5 自定义View的尺寸
比如适配wrap_content，这是一个比较重要的事情。《Android开发艺术探索》任玉刚大佬总结得特别好，套用一下套路。  
![](http://upload-images.jianshu.io/upload_images/2161259-c54135de1a7b3e58.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![](http://upload-images.jianshu.io/upload_images/2161259-c08ad9e54caeddf7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![](http://upload-images.jianshu.io/upload_images/2161259-aa1b842cff80dbc0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
默认尺寸还是用dp最好。

### 2. 自定义点赞view
#### 2.1 资源导入

![](http://upload-images.jianshu.io/upload_images/2161259-33ec1618921f028e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 2.2 动画分析
![](http://upload-images.jianshu.io/upload_images/2161259-fcb1b4d72f81d020.gif?imageMogr2/auto-orient/strip)  
仔细看原效果，左边的View涉及了3张图片。  
每次点击，前一个图片缩小退出，后一个图片放大进入。而点赞+发光两张图片的进入动画略有不同。总结如下：
* 退出动画，时间较短。  
  灰拇指和红拇指和发光都可以用
* 灰拇指进入动画
* 红拇指进入动画  
该动画最后有个回弹抖动，所以和灰拇指区别开来
* 发光进入动画

退出动画都是匀速，灰拇指进入也是匀速，红拇指进入会抖动，发光是扩散放大。这样分成4个动画。

基于以上的分析，new出4个ObjectAnimator对象，并有4个对应的类成员变量对应动画进度。

![](http://upload-images.jianshu.io/upload_images/2161259-a99b0d156c5038aa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
自定义属性使用ObjectAnimator动画的要点是：写好自定义属性的set、get方法。
> Alt + insert Android studio快捷键可以快捷完成这一步

![](http://upload-images.jianshu.io/upload_images/2161259-4bd12f558f14a9aa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**动画顺序**
点赞：灰拇指缩小退出，红拇指和发光一同进入  
取消点赞：红拇指和发光一同退出，灰拇指进入

可以用AnimatorSet来组合动画播放

![](http://upload-images.jianshu.io/upload_images/2161259-a85c1e236a80aa56.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**结合点赞事件**

![还记得我们的Like接口吗？](http://upload-images.jianshu.io/upload_images/2161259-2106f77c6ce2c026.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![](http://upload-images.jianshu.io/upload_images/2161259-87212df75691dbc3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
这里参考了即刻大佬的点评，加入了取消动画的逻辑。

**动画进行中更新UI**
动画的实际是，执行到每一帧，就去触发重新绘制。

![](http://upload-images.jianshu.io/upload_images/2161259-12a4d1053a16e84e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
发光动画认为和红拇指进入动画时间差不多，所以不加监听。

![](http://upload-images.jianshu.io/upload_images/2161259-4987b97ccabc15b0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
动画进行时，重绘。

**具体绘制**
因为涉及点赞、取消点赞的退出进入的动画效果，期间还要调整图片绘制的位置、动画缩放的大小程度等等。所以需要声明一些变量来存储一些可能需要变动的值。

![](http://upload-images.jianshu.io/upload_images/2161259-e603e8d25abf0a36.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
我写的比较乱，待会用到了再来看看注释即可。
首先，需要知道可用于绘制的宽和高。

![](http://upload-images.jianshu.io/upload_images/2161259-d9b7849847ac4b99.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
算出绘制中心能比较省事。而这个边长**squareSideLen**，我觉得图片部分绘制使用正方形是最合适的，因为不希望非正方形绘制区域导致出现拉伸的情况。所以取可绘制区域中心的一个正方形来进行绘制。
![](http://upload-images.jianshu.io/upload_images/2161259-fd08d15ed4367a2d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

图片在绘制时，发光和拇指没有重叠，所以要把拇指往下移动一点，发光往上移动一点。而拇指在缩小的时候，假如发光不往下移动，会感觉二者不是一个整体。  
![](http://upload-images.jianshu.io/upload_images/2161259-d7eb9ce9b4f2db44.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**退出动画进行过程**
红灰拇指大小都是一样的，所以只需要写一次rect的位置计算。绘制中心减去边长的一半就是left和top，绘制中心加上边长的一半就是right和bottom。其中，拇指往下稍稍移动，让出发光的位置，所以都加上**thumbsOffsetY**。

其中**thumbsScale**是拇指部分占可绘制正方形的比例，**minifyScale**是拇指缩小动画最大缩小的比例。  
计算好绘制拇指的rect，就按liked的值进行绘制。
![](http://upload-images.jianshu.io/upload_images/2161259-03d3dac163a38c98.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

退出动画，画好了拇指，再来画发光：
![](http://upload-images.jianshu.io/upload_images/2161259-47f20229aaba3871.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**点赞进入动画**
进入动画在退出动画之后，也就是outProgress<1的时候，所以直接加一个return，让进入动画的逻辑比较整洁。
![](http://upload-images.jianshu.io/upload_images/2161259-e362377103308e04.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

进入动画的拇指部分。1-likeInProgress，这个值会慢慢变小，所以缩小部分会慢慢变小，也就是图形慢慢变大，达到放大的效果。
![](http://upload-images.jianshu.io/upload_images/2161259-c9ffe58983836ea1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
进入动画的发光部分，这是一个小小的特殊的地方。仔细看效果，发光其实是扩散出去的。
![](http://upload-images.jianshu.io/upload_images/2161259-fcb1b4d72f81d020.gif?imageMogr2/auto-orient/strip)  
所以这里我觉得用裁剪画布会比较完美。把发光部分单独拎出来画，然后画布以发光每条线指着的中心为圆心画圆裁剪，半径慢慢变大。这样一点点把发光原图呈现出来，就达到了原图的效果。
裁剪接口里面并不支持圆的裁剪，所以使用path路径裁剪，如下addCircle，我发现path在addCircle之后，需要清空一次，然后再来addCircle，保持整洁。  
![](http://upload-images.jianshu.io/upload_images/2161259-00a304a3f043f1ee.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

一圈淡红色的扩散效果
仔细看，会发现原效果里面有一个圆形放大的扩散。看上面那张，这里不重复再贴原效果图。画圆裁剪都做了，这个也难不倒。  
找好中心，设置好半径，调整一下，也能达到效果。
![](http://upload-images.jianshu.io/upload_images/2161259-f3c5e7ded0ae2cfe.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
**取消点赞进入效果**
拇指部分和点赞进入的红拇指差不多，progress换了一下而已。
![](http://upload-images.jianshu.io/upload_images/2161259-d1c5f0483ba39a2b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
这里还有一个小重点，不过是我猜的。原图的发光在退出时，似乎会稍稍顿一下。为达到这个效果，在灰拇指进入时，进度0.1以前，我们还是绘制一下发光效果。绘制了之后，发现效果不错。
![](http://upload-images.jianshu.io/upload_images/2161259-e1d9cefe36fe2e2d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

到这里，图片部分就绘制完成了。可以在手机上调试一下，对比一下，诶？效果还可以。

#### 2.3 绘制点赞数量部分
参考即刻大佬的点评，把图片和数字部分分离，所以单独写了一个数字部分的自定义。
**自定义属性**
本来想要继承TextView，这样padding什么的都不用考虑。但是想了想，onDraw全被我重写了，gravity我估计不好处理。于是还是继承View来写。  
继承View的话，就需要添加自定义属性了，比如TextSize、TextColor这样的属性等等。
这是attrs.xml里面的属性：
![](http://upload-images.jianshu.io/upload_images/2161259-2c5ab762dc631a16.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
然后我们在构造器里面获取到值，并使用起来。
![](http://upload-images.jianshu.io/upload_images/2161259-69861ed2c77cedf4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
**尺寸问题**
尺寸会比图片要特殊，因为数字可能会比较大，可能有几万个赞。所以把默认大小设置成6位数字，那100万个赞应该是比较难达到了吧。
> 其实百万个赞没准就达到了，这里算我没适配好。小伙伴看了文章之后有兴趣可以试试。我写完这个回顾，也会找时间去完善起来。

这里也还有个小技巧，mPaint是画笔，上面初始化的时候我们把textSize属性设置给了画笔。这里就可以用画笔来告诉我们一个数字的横向大小是多少。**mPaint.measureText("0")**

![](http://upload-images.jianshu.io/upload_images/2161259-b93ee3abafc62ef8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**点赞数量分析**
开始绘制之前，还有一些逻辑需要处理。数字需要一个或者两个或者3个的一起执行动画，所以像TextView那也直接setText肯定达不到效果。需要一个一个数字绘制。所以要把点赞数量每一位都保存起来。而且把上一次这一次的都保存起来。

![](http://upload-images.jianshu.io/upload_images/2161259-27e09b5f7b3ebab6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
诶？这里我居然用了11位数组，默认又使用6位大小，算我不攻自破了。  
这里我还声明了前后点赞数目的位数，什么用后文揭晓。
![](http://upload-images.jianshu.io/upload_images/2161259-fdaaaa0b7d229cfa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
这个是分析方法，Math.pow()是几次幂的意思。这个方法就把新旧点赞数量放到新旧数组里面去了。
**和点赞事件关联**
![](http://upload-images.jianshu.io/upload_images/2161259-ebe0c076699ea666.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![](http://upload-images.jianshu.io/upload_images/2161259-3871be344bb96619.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![](http://upload-images.jianshu.io/upload_images/2161259-a726fc1823fc15a3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
经过图片的动画，这次的动画简直so easy，所以只贴个代码。
**数字的具体绘制**
首先还是绘制区域大小，以及一个数字的宽度。（因为绘制一个数字就得右移一下再绘制下一个数字）
![](http://upload-images.jianshu.io/upload_images/2161259-50c6caa555772e99.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

分两种情况讨论，前后点赞位数不一样时。点赞位数不一样，那就得整个数字一起动画出去，新的动画进来。所以这比较特殊。
![](http://upload-images.jianshu.io/upload_images/2161259-64d8cd025ef7ef4c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
接下来就是另一种情况：前后点赞位数相同，只是其中的数字不一样。这种情况，其实变化部分的位数也是一样的。比如199+1=200，变化是3位；1988+1=1989；变化是1位。所以从左往右，只要不一样的，就执行动画就ok。
![](http://upload-images.jianshu.io/upload_images/2161259-a457bdde0284109c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
### 3. 大功告成
![](http://upload-images.jianshu.io/upload_images/2161259-6f565e8d9f974af8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)  
数字里面有设置具体多少点赞的方法，加一个数字，调试起来看看，nice！
![](http://upload-images.jianshu.io/upload_images/2161259-b8768feb6f18736c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![](http://upload-images.jianshu.io/upload_images/2161259-37921bf0c3078bd9.gif?imageMogr2/auto-orient/strip)

其实参与活动的一些大佬的项目我还没有去拜读。我希望自己写完后再去借鉴。借鉴完没准能知道大佬的奇思妙想。

第一次写个自定义View的回顾文章。里面涉及到的知识点其实我没讲，只是贴出代码告诉大家我用了啥用了啥。但是具体怎么用，看[henCoder](hencoder.com) 追代码看注释看源码 happy，都是非常好的。

简书的代码高亮比较丑，所以贴出来的都是截图。希望看源码的请移步github。
github：[LikeViewDemo](https://github.com/lanzry/LikeViewDemo)

最后，见笑了各位。希望大佬指点一下，希望觉得写得还行的小伙伴点个赞。happy



