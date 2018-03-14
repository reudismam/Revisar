package prosseek;

public class SuperA {
    int i = 0;
    public void foo()
    {
        System.out.println(i);
    }
}

public class B {
    public void bar()
    {
        SuperA a = new SuperA();
        a.foo();
    }
}