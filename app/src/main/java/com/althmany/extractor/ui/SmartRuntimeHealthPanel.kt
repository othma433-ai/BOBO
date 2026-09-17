package com.althmany.extractor.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.althmany.extractor.engine.health.LiveHealthSnapshot
import com.althmany.extractor.engine.health.SmartHealthLevel

@Composable
fun SmartRuntimeHealthPanel(
    rows: List<LiveHealthSnapshot>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF081722),
        border = BorderStroke(1.dp, Color(0xFF183448))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Smart Runtime Health",
                color = Color(0xFF08D9FF),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            rows.forEach { row ->
                val status = when (row.level) {
                    SmartHealthLevel.HEALTHY -> "Healthy"
                    SmartHealthLevel.DEGRADED -> "Degraded"
                    SmartHealthLevel.RECOVERING -> "Recovering"
                    SmartHealthLevel.BLOCKED -> "Blocked"
                }
                Surface(
                    color = Color(0xFF061522),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF183448))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            "${row.owner.name} • $status • ${row.score}/100",
                            color = Color(0xFFF2F7FA),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        Text(
                            "Performance: ${row.performanceMode} • Recovery: ${row.recoveryDirective.name}",
                            color = Color(0xFF91A3B2),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        Text(
                            "UI ${row.uiLatencyMs}ms • Snapshot ${row.snapshotLatencyMs}ms • Failures ${row.consecutiveFailures}",
                            color = Color(0xFF91A3B2),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
