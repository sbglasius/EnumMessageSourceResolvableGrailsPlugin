package grails.plugin.enummessagesourceresolvable.transformation
import grails.plugin.enummessagesourceresolvable.annotations.EnumMessageSourceResolvable
import grails.plugin.enummessagesourceresolvable.bean.EnumMessageSourceResolvableHelper
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.DelegateASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.springframework.context.MessageSourceResolvable

import java.lang.reflect.Modifier

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class EnumMessageSourceResolvableTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = EnumMessageSourceResolvable
    static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS)
    static final String MY_TYPE_NAME = '@' + MY_TYPE.nameWithoutPackage

    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: wrong types: ${nodes[0].class} / ${nodes[1].class}")
        }
        AnnotationNode annotationNode = nodes[0] as AnnotationNode
        def annotatedNode = nodes[1]


        if (annotatedNode instanceof ClassNode) {
            ClassNode classNode = annotatedNode
            addInterface(classNode)
            addHelper(classNode, annotationNode)

        }
    }


    private addInterface(ClassNode classNode) {
        def clazz = ClassHelper.make(MessageSourceResolvable)
        classNode.addInterface(clazz)
    }

    private addHelper(ClassNode classNode, AnnotationNode annotationNode) {
        MapExpression mapExpression = createMapExpression(annotationNode)

        Expression thisExpression = new VariableExpression("this", classNode)
        ArgumentListExpression helperArguments = new ArgumentListExpression(thisExpression, mapExpression)
        Expression helperValue = new ConstructorCallExpression(ClassHelper.make(EnumMessageSourceResolvableHelper), helperArguments)
        FieldNode helperField = new FieldNode('$helper', Modifier.PRIVATE, ClassHelper.make(EnumMessageSourceResolvableHelper), classNode, helperValue)
        AnnotationNode delegateAnnotation = new AnnotationNode(ClassHelper.make(Delegate))

        helperField.addAnnotation(delegateAnnotation)
        classNode.addTransform(DelegateASTTransformation, delegateAnnotation)
        classNode.addField(helperField)
    }

    private MapExpression createMapExpression(AnnotationNode annotationNode) {
        Expression prefix = annotationNode.getMember('prefix')
        Expression postfix = annotationNode.getMember('postfix')
        Expression shortName = annotationNode.getMember('shortName')
        Expression defaultNameCase = annotationNode.getMember('defaultNameCase') ?: new PropertyExpression(new ClassExpression(ClassHelper.make(DefaultNameCase)), new ConstantExpression("UNCHANGED"))

        Expression mapExpression = new MapExpression()
        if (prefix) {
            mapExpression.addMapEntryExpression(createMapEntryExpression("prefix", prefix))
        }
        if (postfix) {
            mapExpression.addMapEntryExpression(createMapEntryExpression("postfix", postfix))
        }
        if (shortName) {
            mapExpression.addMapEntryExpression(createMapEntryExpression("shortName", shortName))
        }
        mapExpression.addMapEntryExpression(createMapEntryExpression("defaultNameCase", defaultNameCase))
        mapExpression
    }

    private MapEntryExpression createMapEntryExpression(String key, Expression value) {
        new MapEntryExpression(new ConstantExpression(key), value)
    }
}
