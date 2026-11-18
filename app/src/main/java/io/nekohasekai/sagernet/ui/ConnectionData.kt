package io.nekohasekai.sagernet.ui

data class ConnectionSnapshot(
    var active: List<ConnectionRecord> = emptyList(),
    var closed: List<ConnectionRecord> = emptyList(),
)

data class ConnectionRecord(
    var id: String = "",
    var createdAt: Long = 0L,
    var closedAt: Long = 0L,
    var upload: Long = 0L,
    var download: Long = 0L,
    var outbound: String = "",
    var outboundType: String = "",
    var network: String = "",
    var source: String = "",
    var destination: String = "",
    var protocol: String = "",
    var domain: String = "",
    var chain: List<String> = emptyList(),
    var processPaths: List<String> = emptyList(),
    var packages: List<String> = emptyList(),
    var ownerUid: Int = 0,
)

data class ConnectionDisplay(
    val record: ConnectionRecord,
    val uploadRate: Long,
    val downloadRate: Long,
    val active: Boolean,
)

fun ConnectionSnapshot.sanitized() = copy(
    active = active.orEmpty().mapNotNull { it?.sanitized() },
    closed = closed.orEmpty().mapNotNull { it?.sanitized() },
)

fun ConnectionRecord.sanitized() = copy(
    id = id.orEmpty(),
    outbound = outbound.orEmpty(),
    outboundType = outboundType.orEmpty(),
    network = network.orEmpty(),
    source = source.orEmpty(),
    destination = destination.orEmpty(),
    protocol = protocol.orEmpty(),
    domain = domain.orEmpty(),
    chain = chain.orEmpty().filterNotNull().filter(String::isNotBlank),
    processPaths = processPaths.orEmpty().filterNotNull().filter(String::isNotBlank),
    packages = packages.orEmpty().filterNotNull().filter(String::isNotBlank),
)

fun ConnectionDisplay.sanitized() = copy(
    record = record.sanitized(),
    uploadRate = uploadRate.coerceAtLeast(0L),
    downloadRate = downloadRate.coerceAtLeast(0L),
)
