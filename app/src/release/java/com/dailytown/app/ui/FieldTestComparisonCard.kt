package com.dailytown.app.ui

import androidx.compose.runtime.Composable
import com.dailytown.app.diagnostics.FieldTestDiagnostic

/**
 * Release builds intentionally contain no field-test comparison/export workflow.
 * The product runtime keeps the call site stable while the debug source set owns the implementation.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
internal fun FieldTestComparisonCard(
    sessionToken: Int,
    canRecordCurrentSession: Boolean,
    buildDiagnostic: () -> FieldTestDiagnostic,
) = Unit
