from diagrams import Diagram, Cluster, Edge
from diagrams.aws.compute import EC2
from diagrams.aws.database import RDS, ElastiCache
from diagrams.aws.devtools import Codedeploy
from diagrams.aws.network import ALB
from diagrams.aws.storage import S3
from diagrams.onprem.vcs import Github
from diagrams.onprem.client import User
from diagrams.onprem.ci import GithubActions
from diagrams.saas.chat import Discord

graph_attr = {
    "fontsize": "16",
    "fontname": "Arial",
    "splines": "ortho",
    "nodesep": "0.8",
    "ranksep": "1.2",
    "pad": "0.8",
    "bgcolor": "#f8f9fa"
}

node_attr = {
    "fontsize": "12",
    "fontname": "Arial",
    "shape": "box",
    "style": "rounded,filled",
    "margin": "0.3,0.1"
}

edge_attr = {
    "fontsize": "9",
    "fontname": "Arial",
    "penwidth": "2",
    "labeldistance": "0.5",
    "labelangle": "0"
}

with Diagram("datagsm-server Cloud Architecture",
             show=False,
             direction="TB",
             graph_attr=graph_attr,
             node_attr=node_attr,
             edge_attr=edge_attr):

    user = User("사용자")
    alb = ALB("Application\nLoad Balancer")

    with Cluster("CI/CD Pipeline", graph_attr={"bgcolor": "#e3f2fd", "style": "rounded", "margin": "10"}):
        github = Github("GitHub\nRepository")
        github_actions = GithubActions("GitHub\nActions")
        s3 = S3("S3 Bucket\n(artifact)")
        codedeploy = Codedeploy("CodeDeploy")

        github >> Edge(label="push/PR", color="#2196f3") >> github_actions
        github_actions >> Edge(label="build artifact", color="#2196f3") >> s3
        s3 >> Edge(label="trigger", color="#2196f3") >> codedeploy

    with Cluster("Monitoring", graph_attr={"bgcolor": "#fff3e0", "style": "rounded", "margin": "10"}):
        discord = Discord("Discord")

        github_actions >> Edge(label="CI/CD 알림", color="#ff9800") >> discord

    with Cluster("AWS", graph_attr={"bgcolor": "#f3e5f5", "style": "rounded", "margin": "15"}):
        with Cluster("Production (EC2)", graph_attr={"bgcolor": "#e8f5e8", "style": "rounded", "margin": "10"}):
            prod_app = EC2(
                "datagsm-web :8080\n"
                "oauth-authorization :8081\n"
                "openapi :8082\n"
                "oauth-userinfo :8083"
            )
            prod_db = RDS("MySQL")
            prod_cache = ElastiCache("Redis")

        with Cluster("Stage (EC2)", graph_attr={"bgcolor": "#e3f2fd", "style": "rounded", "margin": "10"}):
            stage_app = EC2(
                "datagsm-web :8080\n"
                "oauth-authorization :8081\n"
                "openapi :8082\n"
                "oauth-userinfo :8083"
            )
            stage_db = RDS("MySQL")
            stage_cache = ElastiCache("Redis")

    user >> Edge(label="HTTPS", color="#4caf50") >> alb
    alb >> Edge(label="prod", color="#4caf50") >> prod_app
    alb >> Edge(label="stage", color="#03a9f4") >> stage_app

    codedeploy >> Edge(label="deploy", color="#ff5722") >> prod_app
    codedeploy >> Edge(label="deploy", color="#ff5722") >> stage_app

    prod_app >> Edge(label="query", color="#795548") >> prod_db
    prod_app >> Edge(label="cache", color="#9c27b0") >> prod_cache
    prod_app >> Edge(label="시스템 오류 알림", color="#f44336") >> discord

    stage_app >> Edge(label="query", color="#795548") >> stage_db
    stage_app >> Edge(label="cache", color="#9c27b0") >> stage_cache
    stage_app >> Edge(label="시스템 오류 알림", color="#f44336") >> discord
