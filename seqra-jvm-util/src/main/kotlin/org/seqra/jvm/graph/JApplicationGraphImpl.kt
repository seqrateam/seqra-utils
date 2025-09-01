package org.seqra.jvm.graph

import org.seqra.ir.api.jvm.JIRClasspath
import org.seqra.ir.api.jvm.JIRMethod
import org.seqra.ir.api.jvm.cfg.JIRInst
import org.seqra.ir.api.jvm.ext.cfg.callExpr
import org.seqra.ir.impl.features.SyncUsagesExtension

open class JApplicationGraphImpl(
    override val cp: JIRClasspath,
    private val usages: SyncUsagesExtension,
) : JApplicationGraph {
    override fun predecessors(node: JIRInst): Sequence<JIRInst> {
        val graph = node.location.method.flowGraph()
        val predecessors = graph.predecessors(node)
        val throwers = graph.throwers(node)
        return predecessors.asSequence() + throwers.asSequence()
    }

    override fun successors(node: JIRInst): Sequence<JIRInst> {
        val graph = node.location.method.flowGraph()
        val successors = graph.successors(node)
        val catchers = graph.catchers(node)
        return successors.asSequence() + catchers.asSequence()
    }

    override fun callees(node: JIRInst): Sequence<JIRMethod> {
        val callExpr = node.callExpr ?: return emptySequence()
        return sequenceOf(callExpr.method.method)
    }

    override fun callers(method: JIRMethod): Sequence<JIRInst> {
        return usages.findUsages(method).flatMap {
            it.flowGraph().instructions.asSequence().filter { inst ->
                val callExpr = inst.callExpr ?: return@filter false
                callExpr.method.method == method
            }
        }
    }

    override fun entryPoints(method: JIRMethod): Sequence<JIRInst> {
        return method.flowGraph().entries.asSequence()
    }

    override fun exitPoints(method: JIRMethod): Sequence<JIRInst> {
        return method.flowGraph().exits.asSequence()
    }

    override fun methodOf(node: JIRInst): JIRMethod {
        return node.location.method
    }

    override fun statementsOf(method: JIRMethod): Sequence<JIRInst> {
        return method.instList.asSequence()
    }
}
