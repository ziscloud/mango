package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.MathException;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author ash
 */
public class ASTVariable extends PrimaryExpression {

    private String beanName;
    private String propertyPath; // 为""的时候表示没有属性

    public ASTVariable(int i) {
        super(i);
    }

    public ASTVariable(Parser p, int i) {
        super(p, i);
    }

    public void setParameter(String parameter) {
        Pattern p = Pattern.compile(":(\\w+)(\\.\\w+)*");
        Matcher m = p.matcher(parameter);
        checkState(m.matches());
        beanName = m.group(1);
        propertyPath = parameter.substring(m.end(1));
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .a.b.c变为a.b.c
        }
    }

    @Override
    void checkType(TypeContext context) {
        Class<?> type = context.getPropertyType(beanName, propertyPath);
        Node node = this;
        do {
            node = node.jjtGetParent();
        } while (!(node instanceof ASTExpression) && (node instanceof ASTAddNode));
        if (node instanceof ASTExpression) { // 到达根节点都是加法
            if (!Integer.class.equals(type) && !int.class.equals(type) && !String.class.equals(type)) {
                throw new MathException("Variable " + literal() + " need Integer or int or String but " + type.getName());
            }
        } else { // 到达根节点的途中遇到了非加法
            if (!Integer.class.equals(type) && !int.class.equals(type)) {
                throw new MathException("Variable " + literal() + " need Integer or int but " + type.getName());
            }
        }
    }

    @Override
    public Object value(RuntimeContext context) {
        return context.getPropertyValue(beanName, propertyPath);
    }

}