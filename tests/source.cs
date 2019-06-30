#if FEATURE_MANAGED_ETW

// OK if we get this far without an exception, then we can at least write out error messages.
// Set m_provider, which allows this.

m_provider = provider;

#endif

#if !ES_BUILD_STANDALONE

// API available on OS >= Win 8 and patched Win 7.
// Disable only for FrameworkEventSource to avoid recursion inside exception handling.

var osVer = Environment.OSVersion.Version.Major * 10 + Environment.OSVersion.Version.Minor;

if (this.Name != "System.Diagnostics.Eventing.FrameworkEventSource" || osVer >= 62)

#endif
