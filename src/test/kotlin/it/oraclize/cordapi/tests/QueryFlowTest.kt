package it.oraclize.cordapi.tests

import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.setCordappPackages
import net.corda.testing.unsetCordappPackages
import org.junit.After
import org.junit.Before
import org.junit.Test

// TODO(java.lang.IllegalStateException: Missing the '-javaagent' JVM argument. Make sure you run the tests with the Quasar java agent attached to your JVM)
//class QueryFlowTest {
//    lateinit var network: MockNetwork
//    lateinit var a: StartedNode<MockNetwork.MockNode>
//    lateinit var b: StartedNode<MockNetwork.MockNode>
//
//    @Before
//    fun setup() {
//        setCordappPackages("it.oraclize.cordapi.flows")
//        network = MockNetwork()
//        val nodes = network.createSomeNodes(2)
//        a = nodes.partyNodes[0]
//        b = nodes.partyNodes[1]
//        a.internals.registerInitiatedFlow(it.oraclize.cordapi.flows.OraclizeQueryFlow::class.java)
//        b.internals.registerInitiatedFlow(it.oraclize.cordapi.flows.OraclizeQueryFlow::class.java)
//        network.runNetwork()
//    }
//
//    @After
//    fun tearDown() {
//        network.stopNodes()
//        unsetCordappPackages()
//    }
//
//    @Test
//    fun `dummy test`() = Unit
//}