package grails.plugin.enummessagesourceresolvable.helper

import grails.plugin.enummessagesourceresolvable.transformation.DefaultNameCase
import grails.util.Holders
import org.springframework.context.MessageSourceResolvable

import static grails.plugin.enummessagesourceresolvable.transformation.DefaultNameCase.*

class EnumMessageSourceResolvableHelper implements MessageSourceResolvable {
    private Map annotationConfig
    private Enum enumValue

    EnumMessageSourceResolvableHelper(Enum value, Map annotationConfig) {
        this.enumValue = value
        this.annotationConfig = annotationConfig
    }

    @Override
    String[] getCodes() {
        String name = config.shortName ? enumValue.class.simpleName : enumValue.class.name
        String value = enumValue.name()

        [value.toUpperCase(), value, value.toLowerCase()].collect {
            "${prefix}${name}.${it}${postfix}"
        }
    }

    @Override
    Object[] getArguments() {
        []
    }

    @Override
    String getDefaultMessage() {
        switch (config.defaultNameCase as DefaultNameCase) {
            case UPPER_CASE:
                return enumValue.name().toUpperCase()
            case LOWER_CASE:
                return enumValue.name().toLowerCase()
            case CAPITALIZE:
                return enumValue.name().toLowerCase().capitalize()
            case ALL_CAPS:
                return enumValue.name().split('_').collect { it.toLowerCase().capitalize() }.join(' ')
            default:
                enumValue.name()
        }
    }

    private getConfig() {
        (Holders.config?.grails?.plugin?.enummessagesourceresolvable ?: [:]) + annotationConfig
    }

    private getPrefix() {
        String prefix = config.prefix
        return prefix ? prefix + (prefix.endsWith('.') ? '' : '.') : ''
    }

    private getPostfix() {
        String postfix = config.postfix
        return postfix ? (postfix.startsWith('.') ? '' : '.') + postfix : ''
    }
}