package zdrowy.senior.io.domain.agent

data class Agent(
    val id: String,
    val fullName: String,
    val role: AgentRole,
    val phone: String,
    val email: String,
    val specialization: String?
)
