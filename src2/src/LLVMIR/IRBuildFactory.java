package LLVMIR;

/**

 * 1. **单例模式**：这个类使用了单例模式，即在整个程序中只能有一个`IRBuildFactory`实例。这是通过私有的构造函数`private IRBuildFactory(){}`和私有的静态实例`f`来实现的。公共方法`getInstance()`返回这个唯一实例。
 *
 * 2. **计算方法**：有两个`calculate`方法，分别用于整数和浮点数的计算。这两个方法基于传入的操作符`op`来执行相应的数学运算。
 *
 * 3. **获取数组类型**：`getArrayType`方法用于根据给定的维度和元素类型构建数组类型。
 *
 * 4. **全局变量**：`getGlobalArray`方法用于创建全局数组变量。这里有两个版本的方法，一个是基于元素类型和索引，又一个是基于整体的数组类型。
 *
 * 5. **GepInst（GetElementPointer指令）**：有两个与此相关的方法，一个是`buildGepInst`用于在基本块中构建GEP指令，另一个是`getGepInst`用于直接获取GEP指令。
 *
 * 6. **构建数组**：`buildArray`方法用于在基本块中分配数组。
 *
 * 7. **函数和参数**：`getFunction`用于获取一个新的函数，而`getArgument`用于获取函数的参数。
 *
 * 8. **基本块**：`getBasicBlock`方法用于获取函数的新的基本块。
 *
 * 9. **指令**：这里定义了多个与IR指令相关的方法，如`getAllocInst`用于获取分配指令，`getLoadInst`用于获取加载指令，`getStoreInst`用于获取存储指令等。
 *
 * 10. **转换**：`turnType`方法用于转换值的类型，如从整数到浮点数或从1位整数到32位整数。
 *
 * 11. **常量**：`buildI1Number`, `buildNumber`等方法用于构建不同类型的常数。
 *
 * 12. **全局变量**：`buildGlobalVar`方法用于构建一个新的全局变量。
 *
 * 13. **转换指令**：`buildConversionInst`和`getConversionInst`方法用于构建和获取转换指令，如从整数到浮点数的转换。
 *
 * 14. **二进制指令**：`getBinaryInst`和`buildBinaryInst`方法用于获取和构建二进制指令，如加、减、乘、除等。
 *
 * 这个类为IR的构建提供了基础的工具和方法，使得在编译器中生成IR更加方便。
 */
public class IRBuildFactory {
}
