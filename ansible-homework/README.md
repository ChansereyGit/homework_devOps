# Ansible Homework - GCP Instance Management

Create and manage GCP instances with Ansible, automatically updating inventory.

## Structure

```
ansible-homework/
├── ansible.cfg          
├── inventory.ini        
├── playbook/
│   ├── create-instances.yaml
│   └── delete-instances.yaml
└── vars/
    └── config.yaml
```

## Configuration

Edit `vars/config.yaml`:
- Update `google_project_id`
- Update `ssh_username`
- Update paths if needed

## Usage

**Create instances:**
```bash
ansible-playbook playbook/create-instances.yaml
```

**Delete instances:**
```bash
ansible-playbook playbook/delete-instances.yaml
```

**Test connectivity:**
```bash
ansible all -m ping
```

## What it does

1. Creates 2 master nodes (e2-standard-2, 50GB)
2. Creates 2 worker nodes (e2-medium, 40GB)
3. Automatically updates `inventory.ini` with IPs
4. Tests SSH connectivity to all instances
5. Can delete all instances and clean inventory

## Instances Created

**Masters:**
- master01: e2-standard-2, asia-southeast1-c, 50GB
- master02: e2-standard-2, asia-southeast1-c, 50GB

**Workers:**
- worker01: e2-medium, asia-southeast1-a, 40GB
- worker02: e2-medium, asia-southeast1-a, 40GB
