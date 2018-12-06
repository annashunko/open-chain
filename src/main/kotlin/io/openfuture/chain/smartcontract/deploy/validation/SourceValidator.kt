package io.openfuture.chain.smartcontract.deploy.validation

import io.openfuture.chain.smartcontract.core.model.SmartContract
import io.openfuture.chain.smartcontract.deploy.utils.asPackagePath
import io.openfuture.chain.smartcontract.deploy.utils.asResourcePath
import io.openfuture.chain.smartcontract.deploy.validation.Whitelist.isAllowed
import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.ASM6
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SourceValidator(val result: ValidationResult) : ClassVisitor(ASM6) {

    companion object {
        private val superContractName = SmartContract::class.java.name.asResourcePath
        private val log: Logger = LoggerFactory.getLogger(SourceValidator::class.java)
    }


    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        if (null == superName || superName != superContractName) {
            result.addError("Class is not a smart contract. Should inherit a $superContractName class")
        }

        log.debug("CLASS: name-$name, interfaces-$interfaces, signature-$signature, version-$version")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitField(access: Int, name: String?, descriptor: String?, signature: String?, value: Any?): FieldVisitor? {
        validateType(descriptor.let { Type.getType(descriptor)?.className })

        log.debug("FIELD: name-$name, descriptor-$descriptor, signature-$signature")
        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        validateType(descriptor.let { Type.getReturnType(descriptor)?.className })

        log.debug("METHOD: name-$name, descriptor-$descriptor, signature-$signature, exceptions-$exceptions")
        return SourceMethodVisitor()
    }

    private fun validateType(classType: String?, message: String? = null) {
        if (null != classType && !isAllowed(classType)) {
            result.addError(message ?: "$classType is forbidden in the smart contract")
        }
    }

    private inner class SourceMethodVisitor : MethodVisitor(ASM6) {

        override fun visitLocalVariable(name: String?, descriptor: String?, signature: String?, start: Label?, end: Label?, index: Int) {
            if (null != name && "this" != name) {
                validateType(descriptor.let { Type.getType(descriptor)?.className })
            }

            log.debug("LOCAL_VAR: name-$name, descriptor-$descriptor")
            super.visitLocalVariable(name, descriptor, signature, start, end, index)
        }

        override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
            validateType(type?.asPackagePath)

            log.debug("TRY_CATCH: type-$type")
            super.visitTryCatchBlock(start, end, handler, type)
        }

        override fun visitTypeInsn(opcode: Int, type: String?) {
            validateType(type?.asPackagePath)

            log.debug("TYPE: type-$type")
            super.visitTypeInsn(opcode, type)
        }

    }

}
