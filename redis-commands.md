# Redis Cache Monitoring - Quick Reference

## Connection
```bash
redis-cli -h localhost -p 6379 -a "12345"
```

## View all Cache Keys
```bash
redis-cli -h localhost -p 6379 -a "12345" KEYS "*"
```
## View Specific Cache Data
### Get individual student cache
```bash
redis-cli -h localhost -p 6379 -a "12345" GET "student::1"
```
### Get all students cache
```bash
redis-cli -h localhost -p 6379 -a "12345" GET "allStudents"
```
# Shows seconds remaining before auto-expiration
```bash
redis-cli -h localhost -p 6379 -a "12345" TTL "allStudents"
```
### Output: 1847 (seconds) = ~30 minutes

### For individual student
```bash
redis-cli -h localhost -p 6379 -a "12345" TTL "student::1"
```
# Shows all commands being executed
```bash
redis-cli -h localhost -p 6379 -a "12345" MONITOR
```
```bash
redis-cli -h localhost -p 6379 -a "12345" INFO stats
```
#Clear All Cache (Testing Only)
```bash
redis-cli -h localhost -p 6379 -a "12345" FLUSHDB
```