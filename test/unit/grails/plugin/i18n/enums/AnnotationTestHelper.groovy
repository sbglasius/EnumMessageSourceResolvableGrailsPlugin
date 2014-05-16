package grails.plugin.i18n.enums
import groovy.text.GStringTemplateEngine
import groovy.text.TemplateEngine
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter

import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

/**
 * @author ast
 *
 * This class is borrowed from the GContracts project
 */
class AnnotationTestHelper {

	private static final int MAX_NESTED_EXCEPTIONS = 10

	/**
	 * Asserts that the given code closure fails when it is evaluated
	 *
	 * @param code
	 * @return the message of the thrown Throwable
	 */
	protected String shouldFail(Closure code) {
		boolean failed = false
		String result = null
		try {
			code.call()
		}
		catch (GroovyRuntimeException gre) {
			failed = true
			result = ScriptBytecodeAdapter.unwrap(gre).getMessage()
		}
		catch (Throwable e) {
			failed = true
			result = e.getMessage()
		}
		assertTrue("Closure $code should have failed", failed)
		return result
	}

	/**
	 * Asserts that the given code closure fails when it is evaluated
	 * and that a particular exception is thrown.
	 *
	 * @param clazz the class of the expected exception
	 * @param code  the closure that should fail
	 * @return the message of the expected Throwable
	 */
	protected String shouldFail(Class clazz, Closure code) {
		Throwable th
		try {
			code.call()
		} catch (GroovyRuntimeException gre) {
			th = ScriptBytecodeAdapter.unwrap(gre)
		} catch (Throwable e) {
			th = e
		}

		if (th == null) {
			fail("Closure $code should have failed with an exception of type $clazz.name")
		} else if (!clazz.isInstance(th)) {
			fail("Closure $code should have failed with an exception of type $clazz.name, instead got Exception $th")
		}
		return th.getMessage()
	}

	/**
	 * Asserts that the given code closure fails when it is evaluated
	 * and that a particular exception can be attributed to the cause.
	 * The expected exception class is compared recursively with any nested
	 * exceptions using getCause() until either a match is found or no more
	 * nested exceptions exist.
	 * <p/>
	 * If a match is found the error message associated with the matching
	 * exception is returned. If no match was found the method will fail.
	 *
	 * @param clazz the class of the expected exception
	 * @param code  the closure that should fail
	 * @return the message of the expected Throwable
	 */
	protected String shouldFailWithCause(Class clazz, Closure code) {
		Throwable th
		Throwable orig
		int level = 0
		try {
			code.call()
		} catch (GroovyRuntimeException gre) {
			orig = ScriptBytecodeAdapter.unwrap(gre)
			th = orig.getCause()
		} catch (Throwable e) {
			orig = e
			th = orig.getCause()
		}

		while (th && !clazz.isInstance(th) && th != th.getCause() && level < MAX_NESTED_EXCEPTIONS) {
			th = th.getCause()
			level++
		}

		if (orig == null) {
			fail("Closure $code should have failed with an exception caused by type $clazz.name")
		} else if (th == null || !clazz.isInstance(th)) {
			fail("Closure $code should have failed with an exception caused by type $clazz.name, instead found these Exceptions:\n${buildExceptionList(orig)}")
		}
		return th.getMessage()
	}

	private String buildExceptionList(Throwable th) {
		StringBuilder sb = new StringBuilder()
		int level = 0
		while (th) {
			if (level > 1) {
				for (int i = 0; i < level - 1; i++) sb.append("   ")
			}
			if (level > 0) sb.append("-> ")
			if (level > MAX_NESTED_EXCEPTIONS) {
				sb.append("...")
				break
			}
			sb.append(th.getClass().getName()).append(": ").append(th.getMessage()).append("\n")
			if (th == th.getCause()) {
				break
			}
			th = th.getCause()
			level++
		}
		return sb.toString()
	}
}
